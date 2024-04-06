import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
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
  sql,
} from "../db/db.js";
import { queryClient } from "../utils/query-client.js";
import { useRandomId, useRandomInt } from "../utils/math.js";
import { TRPCError } from "@trpc/server";
import {
  HistoricalGame,
  HistoricalGamePlayer,
  HistoricalGamePlayerKit,
} from "wrangler";
import { wranglerClient } from "../services/wrangler-service.js";

export const minigameRouter = router({
  start: internalProcedure
    .input(
      z.object({ playerUuids: z.array(z.string()), minigameId: z.string() })
    )
    .mutation(async ({ input }) => {
      const [minigame, dbPlayerData] = await db.batch([
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

      const playerData = dbPlayerData.map((data) => {
        return {
          ...data,
          selectedKit: {
            ...data.selectedKit,
            passives: data.selectedKit.passives.map((relation) => {
              return {
                ...relation,
                passive: {
                  ...relation.passive,
                  meta: {
                    ...relation.passive.meta,
                    ...relation.meta,
                  },
                },
              };
            }),
          },
        };
      });

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

      let gameId = useRandomId(15);

      // Ensure there are no collisions with game ids even though it's unlikely
      // while (
      //   // query for already existing game ids once that table exists
      // ) {
      //   gameId = useRandomId(15);
      // }

      return {
        gameId,
        minigame,
        players: playerData,
        map: validMaps[mapIndex],
      };
    }),
  end: internalProcedure
    .input(
      z.object({
        gameId: z.string(),
        mapId: z.string(),
        minigameId: z.string(),
        winningUuids: z.array(z.string()),
        players: z.array(
          z.object({
            uuid: z.string(),
            stocksLeft: z.number(),
            kits: z.array(
              z.object({
                id: z.string(),
                startTime: z.number(),
                endTime: z.number(),
              })
            ),
          })
        ),
      })
    )
    .mutation(async ({ input }) => {
      const historicalGameRepository =
        wranglerClient.getRepository(HistoricalGame);

      const historicalGamePlayers = input.players.map((p) => {
        const kits = p.kits.map((k) => {
          return new HistoricalGamePlayerKit(k.id, k.startTime, k.endTime);
        });

        return new HistoricalGamePlayer(p.uuid, p.stocksLeft, kits);
      });

      const wranglerTask = historicalGameRepository.save({
        gameId: input.gameId,
        mapId: input.mapId,
        minigameId: input.minigameId,
        players: historicalGamePlayers,
      });

      const tusslerTask = db.batch([
        db.update(basicPlayerDataTable).set({
          totalGamesPlayed: sql`${basicPlayerDataTable.totalGamesPlayed} + 1`,
        }),
        db
          .update(basicPlayerDataTable)
          .set({
            totalGamesWon: sql`${basicPlayerDataTable.totalGamesWon} + 1`,
          })
          .where(inArray(basicPlayerDataTable.uuid, input.winningUuids)),
      ]);

      await Promise.all([wranglerTask, tusslerTask]);
    }),
});
