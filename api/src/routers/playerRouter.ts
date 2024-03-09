import { z } from "zod";
import { procedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";
import { db } from "db";

export const playerRouter = router({
  getBasicPlayerData: procedure
    .input(z.object({ playerUuid: z.string() }))
    .query(async ({ input }) => {
      return await queryClient.fetchQuery({
        staleTime: Infinity,
        queryKey: ["basicPlayerData", input.playerUuid],
        queryFn: () => {
          return db.query.basicPlayerDataTable.findFirst({
            where: (table, { eq }) => eq(table.uuid, input.playerUuid),
          });
        },
      });
    }),
});
