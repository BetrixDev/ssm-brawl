import { calculateDailyLoginStreak } from "@/helpers/daily-login-streak";
import { incrementStat } from "@/helpers/stats-helpers";
import type { PlayerDocument } from "@/schemas/player-document";
import { getPlayerHeadSkinBase64 } from "@/sdks/mc-heads";
import { randomUUID } from "crypto";
import { eq } from "drizzle-orm";
import { Table } from "..";
import type { Database } from "../index";

export async function getPlayerDocument(db: Database, uuid: string): Promise<PlayerDocument> {
  const queryResult = await db.query.players.findFirst({
    where: (players, { eq }) => eq(players.uuid, uuid),
    with: {
      bans: true,
    },
  });

  if (!queryResult) {
    return createInitialPlayerDocument(db, uuid);
  }

  return {
    isFirstTimeOnServer: false,
    lastJoinDate: queryResult.lastJoinedDate.toISOString(),
    headSkinBase64: queryResult.headSkinBase64,
    dailyLoginStreak: queryResult.dailyLoginStreak ?? null,
    stats: queryResult.stats,
    banData: queryResult.bans.map((ban) => ({
      id: ban.id,
      isBanned: ban.expiresAt ? ban.expiresAt.getTime() > Date.now() : true,
      reason: ban.reason,
      expiresAt: ban.expiresAt?.toISOString() ?? null,
      bannedAt: ban.bannedAt.toISOString(),
      bannedBy: ban.bannedBy,
    })),
  };
}

export async function createInitialPlayerDocument(
  db: Database,
  uuid: string,
): Promise<PlayerDocument> {
  const headSkinBase64 = await getPlayerHeadSkinBase64(uuid);

  const [insertResult] = await db
    .insert(Table.players)
    .values({
      uuid,
      username: uuid,
      lastJoinedDate: new Date(),
      firstJoinedDate: new Date(),
      stats: {},
      headSkinBase64,
    })
    .returning();

  return {
    isFirstTimeOnServer: true,
    lastJoinDate: insertResult.lastJoinedDate.toISOString(),
    headSkinBase64,
    dailyLoginStreak: insertResult.dailyLoginStreak ?? null,
    stats: insertResult.stats,
    banData: null,
  };
}

export async function handlePlayerJoinEvent(db: Database, uuid: string) {
  await db.transaction(async (tx) => {
    await tx.insert(Table.playerJoinEvents).values({
      id: randomUUID(),
      playerUuid: uuid,
      timestamp: new Date(),
    });

    const player = await tx.query.players.findFirst({
      where: (players, { eq }) => eq(players.uuid, uuid),
    });

    await tx
      .update(Table.players)
      .set({
        lastJoinedDate: new Date(),
        dailyLoginStreak: calculateDailyLoginStreak(
          player?.dailyLoginStreak,
          player?.lastJoinedDate ?? null,
        ),
        stats: player?.stats
          ? {
              ...player.stats,
              joinCount: incrementStat(player.stats, "joinCount", 0),
            }
          : undefined,
      })
      .where(eq(Table.players.uuid, uuid));
  });
}

export async function updatePlayerDocument(db: Database, uuid: string, document: PlayerDocument) {
  await db.transaction(async (tx) => {
    await tx
      .update(Table.players)
      .set({
        lastJoinedDate: new Date(document.lastJoinDate),
        dailyLoginStreak: document.dailyLoginStreak ?? undefined,
        stats: document.stats,
      })
      .where(eq(Table.players.uuid, uuid));

    if (document.banData?.length) {
      const existingBans = await tx.query.playerBans.findMany({
        where: (playerBans, { eq }) => eq(playerBans.playerUuid, uuid),
      });

      for (const banData of document.banData) {
        const existingBan = existingBans.find((ban) => ban.id === banData.id);

        if (existingBan) {
          await tx
            .update(Table.playerBans)
            .set({
              reason: banData.reason,
              expiresAt: banData.expiresAt ? new Date(banData.expiresAt) : null, // null for permanent bans
              bannedBy: banData.bannedBy,
            })
            .where(eq(Table.playerBans.id, existingBan.id));
        } else {
          await tx.insert(Table.playerBans).values({
            id: randomUUID(),
            playerUuid: uuid,
            reason: banData.reason,
            expiresAt: banData.expiresAt ? new Date(banData.expiresAt) : null, // null for permanent bans
            bannedAt: new Date(banData.bannedAt),
            bannedBy: banData.bannedBy,
          });
        }
      }
    }
  });
}
