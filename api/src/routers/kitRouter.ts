import { z } from "zod";
import { wranglerClient } from "wrangler";
import { HistoricalGame } from "wrangler/entities/HistoricalGame.js";
import { internalProcedure, router } from "../trpc.js";
import { queryClient } from "../utils.js";

export const kitsRouter = router({
  getKitPlaytimeMillis: internalProcedure
    .input(
      z.object({
        kitId: z.string(),
      }),
    )
    .query(async ({ input }) => {
      const wranglerRes = await queryClient.fetchQuery({
        queryKey: ["kitPlaytimeMillis", input.kitId],
        queryFn: () =>
          wranglerClient
            .aggregate<HistoricalGame, { kitPlaytime: number }>(HistoricalGame, [
              {
                $unwind: "$players",
              },
              {
                $unwind: "$players.kitsUsed",
              },
              {
                $match: {
                  "players.kitsUsed.id": input.kitId,
                },
              },
              {
                $project: {
                  _id: 0,
                  timeElapsed: {
                    $subtract: [
                      "$players.kitsUsed.dateLastUsed",
                      "$players.kitsUsed.dateFirstUsed",
                    ],
                  },
                },
              },
              {
                $group: {
                  _id: 1,
                  kitPlaytime: {
                    $sum: "$timeElapsed",
                  },
                },
              },
            ])
            .tryNext(),
      });

      if (!wranglerRes) {
        return 0;
      }

      return wranglerRes.kitPlaytime;
    }),
});
