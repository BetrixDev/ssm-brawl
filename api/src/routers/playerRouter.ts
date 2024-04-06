import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";
import { basicPlayerDataTable, db, eq } from "../db/db.js";

export const playerRouter = router({
  getBasicPlayerData: internalProcedure
    .input(z.object({ playerUuid: z.string() }))
    .query(async ({ input }) => {
      const response = await queryClient.fetchQuery({
        queryKey: ["basicPlayerData", input.playerUuid],
        queryFn: async () => {
          const playerData = await db.query.basicPlayerDataTable.findFirst({
            where: (table, { eq }) => eq(table.uuid, input.playerUuid),
          });

          if (playerData === undefined) {
            const [newPlayerData] = await db
              .insert(basicPlayerDataTable)
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
  isIpBanned: internalProcedure
    .input(z.object({ ip: z.string(), playerUuid: z.string().optional() }))
    .query(async ({ input }) => {
      const ipEntry = await queryClient.fetchQuery({
        queryKey: ["ipBans", input.ip],
        queryFn: () => {
          return db.query.ipBansTable.findFirst({
            where: (table, { eq }) => eq(table.ip, input.ip),
          });
        },
      });

      if (ipEntry !== undefined && ipEntry.isBanned) {
        if (input.playerUuid !== undefined) {
          try {
            await db
              .update(basicPlayerDataTable)
              .set({ isBanned: true })
              .where(eq(basicPlayerDataTable.uuid, input.playerUuid))
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
