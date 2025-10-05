import { calculateDailyLoginStreak } from "@/helpers/daily-login-streak";
import { eq } from "drizzle-orm";
import { randomUUID } from "node:crypto";
import z from "zod";
import { db, Table } from "../db";

export async function handlePlayerJoinEvent(uuid: string) {
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
        lastSeenOnlineDate: new Date(),
        dailyLoginStreak: calculateDailyLoginStreak(
          player?.dailyLoginStreak,
          player?.lastJoinedDate ?? null,
        ),
      })
      .where(eq(Table.players.uuid, uuid));

    const existingJoinStat = await tx.query.playerGeneralStats.findFirst({
      where: (playerGeneralStats, { eq, and }) =>
        and(eq(playerGeneralStats.playerUuid, uuid), eq(playerGeneralStats.statId, "joinCount")),
    });

    if (existingJoinStat) {
      const joinCount = z.number().safeParse(existingJoinStat.value).data ?? 0;

      await tx
        .update(Table.playerGeneralStats)
        .set({
          value: joinCount + 1,
        })
        .where(eq(Table.playerGeneralStats.playerUuid, uuid));
    } else {
      await tx.insert(Table.playerGeneralStats).values({
        playerUuid: uuid,
        statId: "joinCount",
        value: {
          joinCount: 1,
        },
      });
    }
  });
}

export async function handlePlayerQuitEvent(uuid: string) {
  await db.insert(Table.playerQuitEvents).values({
    id: randomUUID(),
    playerUuid: uuid,
    timestamp: new Date(),
  });
}

export async function getOnlinePlayers() {
  const now = new Date();
  const tenMinutesAgo = new Date(now.getTime() - 10 * 60 * 1000);

  const onlinePlayers = await db.query.players.findMany({
    columns: {
      uuid: true,
      username: true,
      lastSeenOnlineDate: true,
    },
    where: (players, { and, gte, lte }) =>
      and(gte(players.lastSeenOnlineDate, tenMinutesAgo), lte(players.lastSeenOnlineDate, now)),
  });

  return onlinePlayers;
}
