import type { PlayerDocument } from "@/schemas/player-document";
import { getPlayerHeadSkinBase64 } from "@/sdks/mc-heads";
import { randomUUID } from "crypto";
import { eq } from "drizzle-orm";
import { db, Table } from "..";

export async function getPlayerDocument(uuid: string): Promise<PlayerDocument> {
  const queryResult = await db.query.players.findFirst({
    where: (players, { eq }) => eq(players.uuid, uuid),
    with: {
      bans: true,
    },
  });

  if (!queryResult) {
    return createInitialPlayerDocument(uuid);
  }

  return {
    isFirstTimeOnServer: false,
    lastJoinDate: queryResult.lastJoinedDate.toISOString(),
    headSkinBase64: queryResult.headSkinBase64,
    stats: queryResult.stats,
    banData: queryResult.bans.map((ban) => ({
      id: ban.id,
      isBanned: false,
      reason: ban.reason,
      expiresAt: ban.expiresAt?.toISOString() ?? null,
      bannedAt: ban.bannedAt.toISOString(),
      bannedBy: ban.bannedBy,
    })),
  };
}

export async function createInitialPlayerDocument(uuid: string): Promise<PlayerDocument> {
  const [[insertResult], headSkinBase64] = await Promise.all([
    db
      .insert(Table.players)
      .values({
        uuid,
        username: uuid,
        lastJoinedDate: new Date(),
        firstJoinedDate: new Date(),
        stats: {},
      })
      .returning(),
    getPlayerHeadSkinBase64(uuid),
  ]);

  return {
    isFirstTimeOnServer: true,
    lastJoinDate: insertResult.lastJoinedDate.toISOString(),
    headSkinBase64,
    stats: insertResult.stats,
    banData: null,
  };
}

export async function updatePlayerDocument(uuid: string, document: PlayerDocument) {
  await db.transaction(async (tx) => {
    await tx
      .update(Table.players)
      .set({
        lastJoinedDate: new Date(document.lastJoinDate),
        firstJoinedDate: new Date(document.lastJoinDate),
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
              expiresAt: banData.expiresAt ? new Date(banData.expiresAt) : undefined, // undefined for permanent bans
              bannedBy: banData.bannedBy,
            })
            .where(eq(Table.playerBans.id, existingBan.id));
        } else {
          await tx.insert(Table.playerBans).values({
            id: randomUUID(),
            playerUuid: uuid,
            reason: banData.reason,
            expiresAt: banData.expiresAt ? new Date(banData.expiresAt) : undefined, // undefined for permanent bans
            bannedAt: new Date(banData.bannedAt),
            bannedBy: banData.bannedBy,
          });
        }
      }
    }
  });
}
