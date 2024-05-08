import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import {
  db,
  eq,
  minigames,
  inArray,
  basicPlayerData,
  queue,
  and,
  gte,
  lte,
  maps,
  sql,
} from "tussler";
import { queryClient } from "../utils/query-client.js";
import { useRandomId, useRandomInt } from "../utils/math.js";
import { TRPCError } from "@trpc/server";
import { wranglerClient } from "wrangler";
import { HistoricalGame } from "wrangler/entities/HistoricalGame.js";
import { HistoricalGameKit } from "wrangler/models/HistoricalGameKit.js";
import { HistoricalGamePlayer } from "wrangler/models/HistoricalGamePlayer.js";
import { HistoricalGameKitAbilityUse } from "wrangler/models/HistoricalGameKitAbilityUse.js";

export const minigameRouter = router({
  start: internalProcedure
    .input(
      z.object({
        teams: z.array(z.array(z.string())),
        minigameId: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      const [minigame] = await db.batch([
        db.query.minigames.findFirst({
          where: eq(minigames.id, input.minigameId),
        }),
        db.delete(queue).where(
          inArray(
            queue.playerUuid,
            input.teams.flatMap((s) => s),
          ),
        ),
      ]);

      if (minigame === undefined) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      const teamsPlayerData = await Promise.all(
        input.teams.map(async (team) => {
          return await Promise.all(
            team.map(async (uuid) => {
              const data = await db.query.basicPlayerData.findFirst({
                where: (table, { eq }) => eq(table.uuid, uuid),
                with: {
                  selectedKit: {
                    with: {
                      abilities: { with: { ability: true } },
                      passives: { with: { passive: true } },
                      disguise: true,
                    },
                  },
                },
              });

              if (!data) {
                throw new TRPCError({
                  code: "BAD_REQUEST",
                  cause: `Can't find player with uuid ${uuid} in basicPlayerData table`,
                });
              }

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
            }),
          );
        }),
      );

      const validMaps = await queryClient.fetchQuery({
        queryKey: ["maps", "game", minigame.minPlayers, minigame.maxPlayers],
        queryFn: async () => {
          return await db.query.maps.findMany({
            where: and(
              lte(maps.minPlayers, minigame.minPlayers),
              gte(maps.maxPlayers, minigame.maxPlayers),
              eq(maps.role, "game"),
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

      return {
        gameId,
        minigame,
        teams: teamsPlayerData,
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
            leftInProgress: z.boolean(),
            kits: z.array(
              z.object({
                id: z.string(),
                startTime: z.number(),
                endTime: z.number(),
                abilityUsage: z.array(
                  z.object({
                    abilityId: z.string(),
                    usedAt: z.number(),
                    damageDealt: z.number().optional(),
                  }),
                ),
              }),
            ),
          }),
        ),
      }),
    )
    .mutation(async ({ input }) => {
      const historicalGameRepository = wranglerClient.getRepository(HistoricalGame);

      const historicalGamePlayers = input.players.map((p) => {
        const kits = p.kits.map((k) => {
          const abilityUsage = k.abilityUsage.map(
            (a) =>
              new HistoricalGameKitAbilityUse(a.abilityId, a.usedAt, {
                damageDealt: a.damageDealt,
              }),
          );

          return new HistoricalGameKit(k.id, k.startTime, k.endTime, abilityUsage);
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
        db.update(basicPlayerData).set({
          totalGamesPlayed: sql`${basicPlayerData.totalGamesPlayed} + 1`,
        }),
        db
          .update(basicPlayerData)
          .set({
            totalGamesWon: sql`${basicPlayerData.totalGamesWon} + 1`,
          })
          .where(inArray(basicPlayerData.uuid, input.winningUuids)),
      ]);

      await Promise.all([wranglerTask, tusslerTask]);
    }),
  getPlayableGames: internalProcedure.query(async () => {
    const allMinigames = await db.query.minigames.findMany({
      where: (table, { eq }) => eq(table.isHidden, false),
      with: { queueEntries: true },
    });

    return allMinigames.map((minigame) => ({
      id: minigame.id,
      playersInQueue: minigame.queueEntries.length,
    }));
  }),
});
