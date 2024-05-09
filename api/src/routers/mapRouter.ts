import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { db } from "tussler";
import { TRPCError } from "@trpc/server";
import { queryClient } from "../utils/query-client.js";

export const mapRouter = router({
  getMapDetails: internalProcedure
    .input(
      z.object({
        mapId: z.string(),
      }),
    )
    .query(async ({ input }) => {
      const mapData = await queryClient.fetchQuery({
        staleTime: 1000 * 60 * 5,
        queryKey: ["getMapDetails", input.mapId],
        queryFn: async () => {
          const result = await db.query.maps.findFirst({
            where: (table, { eq }) => eq(table.id, input.mapId),
          });

          return result ?? null;
        },
      });

      if (!mapData) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      return mapData;
    }),
});
