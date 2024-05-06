import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";
import { basicPlayerData, db, eq, usercache } from "tussler";
import { TRPCError } from "@trpc/server";

export const playerRouter = router({
  getBasicPlayerData: internalProcedure
    .input(z.object({ playerUuid: z.string() }))
    .query(async ({ input }) => {
      const response = await queryClient.fetchQuery({
        queryKey: ["basicPlayerData", input.playerUuid],
        queryFn: async () => {
          const playerData = await db.query.basicPlayerData.findFirst({
            where: (table, { eq }) => eq(table.uuid, input.playerUuid),
          });

          if (playerData === undefined) {
            const [newPlayerData] = await db
              .insert(basicPlayerData)
              .values([
                {
                  uuid: input.playerUuid,
                  selectedKitId: "creeper",
                },
              ])
              .returning()
              .execute();

            return { ...newPlayerData, firstTime: true };
          }

          return { ...playerData, firstTime: false };
        },
      });

      if (response.firstTime) {
        await queryClient.invalidateQueries({
          queryKey: ["basicPlayerData", input.playerUuid],
        });
      }

      return response;
    }),
  updatePlayerName: internalProcedure
    .input(
      z.object({
        playerUuid: z.string(),
        username: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      const [cacheEntry] = await db
        .insert(usercache)
        .values({
          username: input.username,
          uuid: input.playerUuid,
        })
        .onConflictDoUpdate({
          set: { username: input.username },
          target: usercache.uuid,
        })
        .returning();

      queryClient.setQueryData(["usercache", input.playerUuid], cacheEntry);

      return cacheEntry;
    }),
  getDetailedPlayerData: internalProcedure
    .input(
      z.object({
        playerUuid: z.string(),
      }),
    )
    .query(async ({ input }) => {
      const basicPlayerData = await db.query.basicPlayerData.findFirst({
        where: (table, { eq }) => eq(table.uuid, input.playerUuid),
      });

      const playerUsercache = await queryClient.fetchQuery({
        queryKey: ["usercache", input.playerUuid],
        queryFn: async () => {
          return await db.query.usercache.findFirst({
            where: (table, { eq }) => eq(table.uuid, input.playerUuid),
          });
        },
      });

      if (!basicPlayerData || !playerUsercache) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      const lifetimeWinrate =
        Math.round(
          (basicPlayerData.totalGamesWon / basicPlayerData.totalGamesPlayed) *
            10,
        ) / 10;

      return {
        uuid: basicPlayerData.uuid,
        username: playerUsercache.username,
        career: {
          lifetimeGamesPlayed: basicPlayerData.totalGamesPlayed,
          lifetimeGamesWon: basicPlayerData.totalGamesWon,
          lifetimeWinrate: lifetimeWinrate,
          rankedElo: basicPlayerData.rankElo,
        },
      };
    }),
  isIpBanned: internalProcedure
    .input(z.object({ ip: z.string(), playerUuid: z.string().optional() }))
    .query(async ({ input }) => {
      const ipEntry = await queryClient.fetchQuery({
        queryKey: ["ipBans", input.ip],
        queryFn: () => {
          return db.query.ipBans.findFirst({
            where: (table, { eq }) => eq(table.ip, input.ip),
          });
        },
      });

      if (ipEntry !== undefined && ipEntry.isBanned) {
        if (input.playerUuid !== undefined) {
          try {
            await db
              .update(basicPlayerData)
              .set({ isBanned: true })
              .where(eq(basicPlayerData.uuid, input.playerUuid))
              .execute();
          } catch (e) {
            console.log(e);
          }
        }

        return {
          isBanned: true,
        };
      } else {
        return {
          isBanned: false,
        };
      }
    }),
});
