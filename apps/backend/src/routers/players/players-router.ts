import { searchPlayerUsernames } from "@/db/queries/search-player-usernames";
import { eventPublisher } from "@/lib/event-publisher";
import { o } from "@/lib/orpc";
import { z } from "zod";

export const playersRouter = {
  searchPlayerUsernames: o
    .input(
      z.object({
        query: z.string(),
      }),
    )
    .handler(async ({ input }) => {
      return await searchPlayerUsernames(input.query);
    }),
  livePlayerCount: o.handler(async function* ({ signal }) {
    for await (const event of eventPublisher.subscribe("updatePlayerCount", { signal })) {
      yield {
        playerCount: event.playerCount,
      };
    }
  }),
};
