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

app.post("/players/:uuid/document/ensure", async (c) => {
  const { uuid } = c.req.param();
  const username = c.req.query("username") ?? "Unknown";

  await c.env.runMutation(internal.plugin.players.document.ensurePlayerDocument, {
    uuid,
    username,
  });

  return c.json({ status: "ok" });
});

app.get("/players/:uuid/kit", async (c) => {
  const { uuid } = c.req.param();

  const selectedKitId = await c.env.runQuery(internal.plugin.players.document.getSelectedKit, {
    uuid,
  });

  return c.json({ selectedKitId });
});

app.post("/players/:uuid/kit", async (c) => {
  const { uuid } = c.req.param();
  const { kitId } = await c.req.json();

  await c.env.runMutation(internal.plugin.players.document.updateSelectedKit, {
    uuid,
    kitId,
  });

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

app.post("/server/status", async (c) => {
  const { playerCount, tps, memoryUsageMb, loadedChunks, loadedWorlds, averagePlayerPing } =
    await c.req.json();

  await c.env.runMutation(internal.plugin.status.updateServerStatus, {
    playerCount,
    tps,
    memoryUsageMb,
    loadedChunks,
    loadedWorlds,
    averagePlayerPing,
  });

  return c.json({ status: "ok" });
});

app.post("/server/status/sync-online-players", async (c) => {
  const { onlinePlayerUuids } = await c.req.json();

  await c.env.runMutation(internal.plugin.status.syncOnlinePlayerCount, {
    onlinePlayerUuids,
  });

  return c.json({ status: "ok" });
});

const http = new HttpRouterWithHono(app);

authComponent.registerRoutes(http, createAuth);

export default http;
