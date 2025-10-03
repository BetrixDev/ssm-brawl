import { searchPlayerUsernames } from "@/db/queries/search-player-usernames";
import { o } from "@/lib/orpc";
import { paginationOptionsSchema } from "@/schemas/pagination-options";
import { z } from "zod";

export const playersRouter = {
  searchPlayerUsernames: o
    .input(
      z.object({
        query: z.string(),
        paginationOptions: paginationOptionsSchema.optional(),
      }),
    )
    .handler(async ({ input }) => {
      return await searchPlayerUsernames(input.query, input.paginationOptions);
    }),
};
