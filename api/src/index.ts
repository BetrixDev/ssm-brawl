import { Hono } from "hono";
import { trpcServer } from "@hono/trpc-server";
import { serve } from "@hono/node-server";
import { logger } from "hono/logger";
import { appRouter } from "./routers/router.js";
import { get } from "lodash-es";
import { t, TrpcContext } from "./trpc.js";
import { renderTrpcPanel } from "trpc-panel";
import { TRPCError } from "@trpc/server";
import { getHTTPStatusCodeFromError } from "@trpc/server/http";
import { env } from "env";
import {
  BackendSource,
  decodeTokenFromHeaders,
  generateBackendToken,
} from "./db/jwt.js";
import { WranglerDataSource } from "wrangler";

const app = new Hono();

app.use(logger());

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
  })
);

app.all("/api/*", async (c) => {
  const claims = await decodeTokenFromHeaders(c.req.raw.headers);

  // Only the plugin should ever be calling this endpoint
  if (claims === undefined || claims.source !== "plugin") {
    c.status(403);
    return c.json({ message: "Forbidden" });
  }

  const caller = t.createCallerFactory(appRouter)({
    claims,
    resHeaders: c.res.headers,
  });

  const paths = c.req.url.split("/").at(-1)!;
  const routerProcedure = await get(caller, paths);

  if (routerProcedure === undefined) {
    return c.notFound();
  }

  let routerResponse: unknown;

  try {
    const requestBody = await c.req.json().catch(() => undefined);
    routerResponse = await routerProcedure(requestBody);
  } catch (e: unknown) {
    if (e instanceof TRPCError) {
      const statusCode = getHTTPStatusCodeFromError(e);
      c.status(statusCode as any);
    } else {
      console.log(e);
      c.status(500);
    }

    return c.json(e);
  }

  c.status(200);
  return c.json(routerResponse);
});

app.get("/panel", async (c) => {
  return c.html(
    renderTrpcPanel(appRouter, { url: "http://localhost:3000/trpc" })
  );
});

// Used when the server intially starts up to generate a token for the plugin
app.post("/generateToken/:source", async (c) => {
  try {
    const source = BackendSource.parse(c.req.param("source"));
    const secret = c.req.header("Secret");

    if (secret !== env.API_TOKEN_SECRET) {
      return c.status(403);
    }

    const token = await generateBackendToken(source);

    c.header("Set-Cookie", `token=${token}; HttpOnly`);

    c.status(200);

    return c.json({
      token,
    });
  } catch {
    return c.status(400);
  }
});

serve({ ...app, port: env.PORT ?? 3000 }, async (info) => {
  await WranglerDataSource.initialize();

  console.log(`Backend listening on http://localhost:${info.port}`);
});
