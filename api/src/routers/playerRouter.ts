import { z } from "zod";
import { procedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";
import { basicPlayerDataTable, db, eq } from "../db/db.js";

export const playerRouter = router({
  getBasicPlayerData: procedure
    .input(z.object({ playerUuid: z.string() }))
    .query(async ({ input }) => {
      return await queryClient.fetchQuery({
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
    }),
  isIpBanned: procedure
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
