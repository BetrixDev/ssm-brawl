import {
  and,
  db,
  eq,
  gte,
  lte,
  mapsTable,
  minigamesTable,
  queueTable,
} from "db";
import * as v from "valibot";

const bodySchema = v.object({
  playerUuid: v.string(),
  minigameId: v.string(),
});

export default defineEventHandler(async (event) => {
  const body = v.parse(bodySchema, await readBody(event));

  const data = await db.query.minigamesTable.findFirst({
    where: eq(minigamesTable.id, body.minigameId),
    with: { queueEntries: true },
  });

  if (data === undefined) {
    setResponseStatus(event, 400);
    return;
  }

  setResponseStatus(event, 200);

  const playerInQueue = data.queueEntries.length;

  if (playerInQueue + 1 >= data.minPlayers) {
    const queuedPlayers = data.queueEntries.map((q) => q.playerId);

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

    return {
      action: "start_game",
      players: [body.playerUuid, ...queuedPlayers],
      minigameId: data.id,
      map: validMaps[mapIndex],
    };
  }

  await db
    .insert(queueTable)
    .values([{ minigameId: data.id, playerId: body.playerUuid }]);
});
