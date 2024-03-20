import { z } from "zod";
import { procedure, router } from "../trpc.js";
import {
  db,
  eq,
  minigamesTable,
  inArray,
  basicPlayerDataTable,
  queueTable,
  and,
  gte,
  lte,
  mapsTable,
} from "../db/db.js";
import { queryClient } from "../utils/query-client.js";
import { generateRandomInt32, useRandomInt } from "../utils/math.js";
import { TRPCError } from "@trpc/server";

export const minigameRouter = router({
  start: procedure
    .input(
      z.object({ playerUuids: z.array(z.string()), minigameId: z.string() })
    )
    .mutation(async ({ input }) => {
      const [minigame, playerData] = await db.batch([
        db.query.minigamesTable.findFirst({
          where: eq(minigamesTable.id, input.minigameId),
        }),
        db.query.basicPlayerDataTable.findMany({
          where: inArray(basicPlayerDataTable.uuid, input.playerUuids),
          with: {
            selectedKit: {
              with: {
                abilities: { with: { ability: true } },
                passives: { with: { passive: true } },
                disguise: true,
              },
            },
          },
        }),
        db
          .delete(queueTable)
          .where(inArray(queueTable.playerUuid, input.playerUuids)),
      ]);

      if (minigame === undefined) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      const validMaps = await queryClient.fetchQuery({
        queryKey: [
          "mapsTable",
          "game",
          minigame.minPlayers,
          minigame.maxPlayers,
        ],
        queryFn: async () => {
          return await db.query.mapsTable.findMany({
            where: and(
              lte(mapsTable.minPlayers, minigame.minPlayers),
              gte(mapsTable.maxPlayers, minigame.maxPlayers),
              eq(mapsTable.role, "game")
            ),
            with: {
              spawnPoints: true,
              origin: true,
            },
          });
        },
      });

      const mapIndex = useRandomInt(0, validMaps.length - 1);

      return {
        gameId: generateRandomInt32(),
        minigame,
        players: playerData,
        map: validMaps[mapIndex],
      };
    }),
});
