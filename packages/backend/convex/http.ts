import { HonoWithConvex, HttpRouterWithHono } from "convex-helpers/server/hono";
import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { logger } from "hono/logger";
import stripAnsi from "strip-ansi";
import { internal } from "./_generated/api";
import { ActionCtx } from "./_generated/server";
import { authComponent, createAuth } from "./auth";

const app: HonoWithConvex<ActionCtx> = new Hono();

const token = process.env.HTTP_AUTH_TOKEN ?? "change-me";

app.use(
  "*",
  logger((...args) => {
    console.log(...args.map(stripAnsi));
  }),
);

app.use("*", bearerAuth({ token }));

app.get("/hc", async (c) => {
  return c.json({ status: "ok" });
});

app.get("/players/:uuid/document", async (c) => {
  const { uuid } = c.req.param();
  const username = c.req.query("username") ?? "Unknown";

  let document = await c.env.runQuery(internal.plugin.players.document.getPlayerDocument, {
    uuid,
  });

  if (!document) {
    document = await c.env.runMutation(internal.plugin.players.document.createPlayerDocument, {
      uuid,
      username,
    });
  }

  return c.json(document);
});

app.put("/players/:uuid/document", async (c) => {
  return c.json({ status: "ok" });
});

app.post("/players/:uuid/events/join", async (c) => {
  const { uuid } = c.req.param();

  await c.env.runMutation(internal.plugin.players.events.onPlayerJoin, {
    uuid,
  });

  return c.json({ status: "ok" });
});

app.post("/players/:uuid/events/leave", async (c) => {
  const { uuid } = c.req.param();

  await c.env.runMutation(internal.plugin.players.events.onPlayerLeave, {
    uuid,
  });

  return c.json({ status: "ok" });
});

const http = new HttpRouterWithHono(app);

authComponent.registerRoutes(http, createAuth);

export default http;
