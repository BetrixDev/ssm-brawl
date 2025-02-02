import { Hono } from "hono";
import { trpcServer } from "@hono/trpc-server";
import { serve } from "@hono/node-server";
import { appRouter } from "./routers/router.js";
import { router, t, TrpcContext } from "./trpc.js";
import { renderTrpcPanel } from "trpc-panel";
import { TRPCError } from "@trpc/server";
import { getHTTPStatusCodeFromError } from "@trpc/server/http";
import { env } from "env/api";
import { BackendSource, decodeTokenFromHeaders, generateBackendToken } from "./jwt.js";
import { wranglerDataSource } from "wrangler";
import { log } from "./log.js";
import { middlewareLogger } from "logger";
import { db, initTussler, runMigrations } from "tussler";
import { loadDefaultKvValues } from "./kv.js";
import { get } from "./utils.js";

const app = new Hono();

app.use(middlewareLogger(log));

app.get("/health", async (c) => {
  c.status(200);
  return c.text("Service is running");
});

app.use(
  "/trpc/*",
  trpcServer({
    router: appRouter,
    createContext: async ({ req, resHeaders }): Promise<TrpcContext> => {
      const claims = await decodeTokenFromHeaders(req.headers);

      if (claims === undefined) {
        throw new TRPCError({ code: "UNAUTHORIZED" });
      }

      return {
        claims,
        resHeaders,
      };
    },
  }),
);

app.all("/api/*", async (c) => {
  const claims = await decodeTokenFromHeaders(c.req.raw.headers);

  // Only the plugin should ever be calling this endpoint
  if (claims === undefined || claims.source !== "plugin") {
    c.status(401);
    return c.json({ message: "Unauthorized" });
  }

  const caller = t.createCallerFactory(appRouter)({
    claims,
    resHeaders: c.res.headers,
  });

  const paths = c.req.url.split("/").at(-1)!;
  const routerProcedure = get(caller, paths);

  let routerResponse: unknown;

  try {
    const requestBody = (c as any).jsonPayload;
    routerResponse = await (routerProcedure as any)(requestBody);
  } catch (e: unknown) {
    if (e instanceof TypeError) {
      return c.notFound();
    }

    log.error({ ...(e as any), path: c.req.path, body: (c as any).jsonPayload });

    if (e instanceof TRPCError) {
      const statusCode = getHTTPStatusCodeFromError(e);
      c.status(statusCode as any);
    } else {
      c.status(500);
    }

    return c.json(e);
  }

  c.status(200);
  return c.json(routerResponse);
});

app.get("/panel", async (c) => {
  return c.html(
    renderTrpcPanel(appRouter, {
      url: `${env.API_PROTOCOL}://${env.API_HOST}:${env.API_PORT}/trpc`,
    }),
  );
});

// Used when the server intially starts up to generate a token for the plugin
app.post("/generateToken/:source", async (c) => {
  try {
    const source = BackendSource.parse(c.req.param("source"));
    const secret = c.req.header("Secret");

    if (secret !== env.API_TOKEN_SECRET) {
      c.status(403);
      return c.text("bad token");
    }

    const token = await generateBackendToken(source);

    c.header("Set-Cookie", `token=${token}; path=/`);

    c.status(200);
    return c.text(token);
  } catch {
    c.status(400);
    return c.text("dont");
  }
});

serve({ ...app, port: env.API_PORT }, async (info) => {
  await initTussler();
  await runMigrations();
  await loadDefaultKvValues();

  /* c8 ignore next 3 */
  if (env.NODE_ENV !== "test") {
    await wranglerDataSource.initialize();
  }

  log.info(`Backend listening on http://localhost:${info.port}`);
});
