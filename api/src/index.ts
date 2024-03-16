import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { trpcServer } from "@hono/trpc-server";
import { serve } from "@hono/node-server";
import { logger } from "hono/logger";
import { appRouter } from "./routers/router.js";
import { get } from "lodash-es";
import { t } from "./trpc.js";
import { renderTrpcPanel } from "trpc-panel";
import { TRPCError } from "@trpc/server";
import { getHTTPStatusCodeFromError } from "@trpc/server/http";
import { env } from "env";

const app = new Hono();

app.use(logger());
app.use("/api/*", bearerAuth({ token: env.API_AUTH_TOKEN }));
app.use("/trpc/*", bearerAuth({ token: env.API_AUTH_TOKEN }));

app.use(
  "/trpc/*",
  trpcServer({
    router: appRouter,
  })
);

const caller = t.createCallerFactory(appRouter)({});

app.all("/api/*", async (c) => {
  const paths = c.req.url.split("/").at(-1)!;
  const routerProcedure = await get(caller, paths);

  if (routerProcedure === undefined) {
    return c.notFound();
  }

  let routerResponse: unknown;

  try {
    const requestBody = await c.req.json();
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

serve(app, (info) => {
  console.log(`Backend listening on http://localhost:${info.port}`);
});
