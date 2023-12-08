import { z } from "zod";
import { and, db, eq, minigamesTables, queueTable } from "@/db";
import { isAuthedForRequest } from "@/utils";

const requestSchema = z.object({
  uuid: z.string(),
  modeId: z.string(),
  isRanked: z.boolean().default(false),
});

export async function POST(request: Request) {
  if (!isAuthedForRequest(request)) {
    return new Response(undefined, {
      status: 403,
    });
  }

  const body = requestSchema.parse(await request.json());

  const [existingPlayerInQueue] = await db
    .select()
    .from(queueTable)
    .where(eq(queueTable.playerUuid, body.uuid))
    .execute();

  if (existingPlayerInQueue !== undefined) {
    return new Response(undefined, {
      status: 409,
    });
  }

  const [miniGame] = await db
    .select()
    .from(minigamesTables)
    .where(eq(minigamesTables.modeId, body.modeId))
    .execute();

  if (miniGame === undefined || (!miniGame.canBeRanked && body.isRanked)) {
    return new Response(undefined, {
      status: 400,
    });
  }

  const currentQueue = await db
    .select()
    .from(queueTable)
    .where(
      and(
        eq(queueTable.modeId, body.modeId),
        eq(queueTable.isRanked, body.isRanked)
      )
    )
    .execute();

  await db
    .insert(queueTable)
    .values({
      modeId: body.modeId,
      isRanked: body.isRanked,
      playerUuid: body.uuid,
    })
    .execute();

  if (currentQueue.length === miniGame.playerCount - 1) {
    // When the player gets added to the queue, there will be enough players to start a game
    // Eventually add a check for the ranked mode to wait a little longer for a more even match

    return new Response(
      JSON.stringify({
        action: "startGame",
        playerUuids: [...currentQueue.map((q) => q.playerUuid), body.uuid],
        modeId: miniGame.modeId,
        isRanked: body.isRanked,
      })
    );
  }

  return new Response(
    JSON.stringify({
      action: "addedPlayerToQueue",
      playerUuid: body.uuid,
      modeId: miniGame.modeId,
      isRanked: body.isRanked,
    })
  );
}
