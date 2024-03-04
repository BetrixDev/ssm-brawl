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
import { useRandomInt } from "../../utils/math";

const bodySchema = v.object({
  playerUuids: v.array(v.string()),
  minigameId: v.string(),
});

export default defineEventHandler(async (event) => {
  const body = v.parse(bodySchema, await readBody(event));

  const [minigame, playerData] = await Promise.all([
    db.query.minigamesTable.findFirst({
      where: eq(minigamesTable.id, body.minigameId),
    }),
    db.query.basicPlayerDataTable.findMany({
      where: inArray(basicPlayerDataTable.uuid, body.playerUuids),
      with: {
        selectedKit: {
          with: {
            abilities: { with: { ability: true } },
            passives: { with: { passive: true } },
          },
        },
      },
    }),
    db
      .delete(queueTable)
      .where(inArray(queueTable.playerUuid, body.playerUuids)),
  ]);

  const queryClient = useQueryClient();

  const validMaps = await queryClient.fetchQuery({
    queryKey: ["mapsTable", minigame.minPlayers, minigame.maxPlayers],
    queryFn: async () => {
      return await db.query.mapsTable.findMany({
        where: and(
          lte(mapsTable.minPlayers, minigame.minPlayers),

          gte(mapsTable.maxPlayers, minigame.maxPlayers)
        ),
        with: {
          spawnPoints: true,
        },
      });
    },
  });

  const mapIndex = useRandomInt(0, validMaps.length - 1);

  return {
    players: playerData,
    map: validMaps[mapIndex],
  };
});
