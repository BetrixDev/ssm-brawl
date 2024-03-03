import {
  and,
  basicPlayerDataTable,
  db,
  eq,
  gte,
  inArray,
  lte,
  mapsTable,
  minigamesTable,
  queueTable,
} from "db";
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

  const data = await db.query.minigamesTable.findFirst({
    where: eq(minigamesTable.id, body.minigameId),
    with: { queueEntries: true },
  });

  if (data === undefined) {
    setResponseStatus(event, 400);
    return;
  }

  setResponseStatus(event, 200);

  const playersInQueue = data.queueEntries.length;

  if (playersInQueue + 1 >= data.minPlayers) {
    const queuedPlayers = data.queueEntries.map((q) => q.playerUuid);

    const queryClient = useQueryClient();

    const validMaps = await queryClient.fetchQuery({
      queryKey: ["mapsTable", data.minPlayers, data.maxPlayers],
      queryFn: async () => {
        return await db.query.mapsTable.findMany({
          where: and(
            lte(mapsTable.minPlayers, data.minPlayers),
            gte(mapsTable.maxPlayers, data.maxPlayers)
          ),
          with: {
            spawnPoints: true,
          },
        });
      },
    });

    const mapIndex = useRandomInt(0, validMaps.length - 1);

    const playerData = await db.query.basicPlayerDataTable.findMany({
      where: inArray(basicPlayerDataTable.uuid, [
        body.playerUuid,
        ...queuedPlayers,
      ]),
      with: {
        selectedKit: true,
      },
    });

    return {
      action: "start_game",
      players: playerData,
      minigameId: data.id,
      map: validMaps[mapIndex],
    };
  }

  return {
    action: "added",
    playersInQueue: playersInQueue + 1,
  };
});
