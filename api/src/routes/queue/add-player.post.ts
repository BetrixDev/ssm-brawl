import { db, eq, minigamesTable, queueTable } from "db";
import * as v from "valibot";

const bodySchema = v.object({
  playerUuid: v.string(),
  minigameId: v.string(),
  force: v.optional(v.boolean()),
});

export default defineEventHandler(async (event) => {
  const body = v.parse(bodySchema, await readBody(event));

  if (body.force) {
    await db
      .delete(queueTable)
      .where(eq(queueTable.playerUuid, body.playerUuid));
  } else {
    const playerInQueue = await db.query.queueTable.findFirst({
      where: eq(queueTable.playerUuid, body.playerUuid),
    });

    if (playerInQueue !== undefined) {
      setResponseStatus(event, 409);
      return;
    }
  }

  const minigameData = await db.query.minigamesTable.findFirst({
    where: eq(minigamesTable.id, body.minigameId),
    with: { queueEntries: true },
  });

  if (minigameData === undefined) {
    setResponseStatus(event, 400);
    return;
  }

  setResponseStatus(event, 200);

  const playersInQueue = minigameData.queueEntries.length;

  if (playersInQueue + 1 >= minigameData.minPlayers) {
    const queuedPlayers = minigameData.queueEntries.map((q) => q.playerUuid);

    return {
      action: "start_game",
      playerUuids: [body.playerUuid, ...queuedPlayers],
      minigameId: minigameData.id,
    };
  }

  return {
    action: "added",
    playersInQueue: playersInQueue + 1,
  };
});
