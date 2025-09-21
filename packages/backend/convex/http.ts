import { zValidator } from "@hono/zod-validator";
import { HonoWithConvex, HttpRouterWithHono } from "convex-helpers/server/hono";
import { Hono } from "hono";
import { bearerAuth } from "hono/bearer-auth";
import { timing } from "hono/timing";
import { api, internal } from "./_generated/api";
import { ActionCtx } from "./_generated/server";
import { PlayerDocument, playerDocumentSchema } from "./schemas";

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

app.get("/players/:uuid/document", async (c) => {
  try {
    const uuid = c.req.param("uuid");
    const isJoinEvent = c.req.query("joinEvent") === "true";

    let playerData = await c.env.runQuery(internal.players.getPlayerByUuid, { uuid });

    const isFirstTimeOnServer = !playerData;

    if (isFirstTimeOnServer) {
      try {
        await c.env.runAction(internal.players.createPlayer, {
          uuid,
        });
      } catch (error) {
        if (error instanceof Error && error.message.includes("Player not found on playerdb.co")) {
          return c.json({ message: `"${uuid}" is not a valid minecraft uuid` }, { status: 404 });
        }

        return c.json({ message: "Player data was not able to be created" }, { status: 500 });
      }
    } else if (isJoinEvent) {
      await c.env.runMutation(internal.players.updatePlayerJoin, { uuid });

      if (playerData) {
        playerData = {
          ...playerData,
          lastJoinDate: new Date().toISOString(),
          stats: {
            ...playerData.stats,
            joinCount: (playerData.stats.joinCount as number) || 0 + 1,
          },
        };
      }
    }

    playerData ??= await c.env.runQuery(internal.players.getPlayerByUuid, { uuid });

    if (!playerData) {
      return c.json({ message: "Player data was not able to be created" }, { status: 500 });
    }

    const banData = await c.env.runQuery(internal.players.getPlayerBanByUuid, { uuid });

    const document: PlayerDocument = {
      avatarUrl: playerData.avatarUrl,
      isFirstTimeOnServer,
      lastJoinDate: playerData.lastJoinDate,
      stats: playerData.stats,
      banData: banData
        ? {
            isBanned: true,
            reason: banData.reason,
            expiresAt: banData.expiresAt,
            bannedAt: banData.bannedAt,
            bannedBy: banData.bannedBy,
          }
        : null,
    };

    return c.json(document);
  } catch (error) {
    return c.json(
      {
        message: "Player document was not able to be fetched",
        error,
      },
      { status: 500 },
    );
  }
});

app.put("/players/:uuid/document", zValidator("json", playerDocumentSchema), async (c) => {
  try {
    const uuid = c.req.param("uuid");
    const document = c.req.valid("json");

    await c.env.runMutation(internal.players.savePlayerDocument, {
      uuid: uuid as any,
      document,
    });

    return c.json({ message: "Player document saved" });
  } catch (error) {
    return c.json(
      {
        message: "Player document was not able to be saved",
        error,
      },
      { status: 500 },
    );
  }
});

app.delete("/players/:uuid/document", async (c) => {
  try {
    const uuid = c.req.param("uuid");
    await c.env.runMutation(internal.players.deletePlayerDocument, { uuid: uuid as any });

    return c.json({ message: "Player data deleted successfully" });
  } catch (error) {
    return c.json(
      {
        message: "Player data was not able to be deleted",
        error,
      },
      { status: 500 },
    );
  }
});

export default new HttpRouterWithHono(app);
