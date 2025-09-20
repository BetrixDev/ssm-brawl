import { HonoWithConvex, HttpRouterWithHono } from "convex-helpers/server/hono";
import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { timing } from "hono/timing";
import { api, internal } from "./_generated/api";
import { ActionCtx } from "./_generated/server";

const app: HonoWithConvex<ActionCtx> = new Hono();

const token = process.env.HTTP_AUTH_TOKEN!!;

if (!token) {
  throw new Error("HTTP_AUTH_TOKEN is not set");
}

app.use(timing());

app.get("/health", (c) => {
  return c.json({ message: "OK" });
});

app.use("/*", bearerAuth({ token }));

app.get("/kv/:key", async (c) => {
  const key = c.req.param("key");
  const value = await c.env.runQuery(api.kv.getKv, { key });

  if (value === undefined) {
    return c.json({ error: "Key not found" }, 404);
  }
  return c.json({ key, value });
});

app.post("/kv", async (c) => {
  const { key, value } = await c.req.json();

  if (!key || value === undefined) {
    return c.json({ error: "key and value are required" }, 400);
  }

  await c.env.runMutation(internal.kv.setKv, { key, value });

  return c.json({ message: "Key-Value pair updated successfully", key, value });
});

app.put("/kv/:key", async (c) => {
  const key = c.req.param("key");
  const { value } = await c.req.json();

  if (value === undefined) {
    return c.json({ error: "key and value are required" }, 400);
  }

  await c.env.runMutation(internal.kv.setKv, { key, value });

  return c.json({ message: "Key-Value pair updated successfully", key, value });
});

app.get("/players/:uuid/joinData", async (c) => {
  const uuid = c.req.param("uuid");

  const playerData = await c.env.runQuery(api.players.getPlayerByUuid, { uuid });
  let firstTimeJoin = false;

  if (!playerData) {
    firstTimeJoin = true;

    await c.env.runAction(internal.players.createPlayer, {
      uuid,
    });
  } else {
    await c.env.runMutation(internal.players.updatePlayerJoin, { uuid });
  }

  return c.json({
    firstTimeJoin,
    playerData: firstTimeJoin ? null : playerData,
  });
});

export default new HttpRouterWithHono(app);
