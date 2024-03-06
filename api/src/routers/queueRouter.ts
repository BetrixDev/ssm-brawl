import { z } from "zod";
import { procedure, router } from "../trpc.js";
import { db, queueTable, eq, minigamesTable } from "db";
import { TRPCError } from "@trpc/server";

export const queueRouter = router({
  addPlayer: procedure
    .input(
      z.object({
        playerUuid: z.string(),
        minigameId: z.string(),
        force: z.boolean().optional(),
      })
    )
    .mutation(async ({ input }) => {
      if (input.force) {
        await db
          .delete(queueTable)
          .where(eq(queueTable.playerUuid, input.playerUuid));
      } else {
        const playerInQueue = await db.query.queueTable.findFirst({
          where: eq(queueTable.playerUuid, input.playerUuid),
        });

        if (playerInQueue !== undefined) {
          throw new TRPCError({ code: "CONFLICT" });
        }
      }

      const minigameData = await db.query.minigamesTable.findFirst({
        where: eq(minigamesTable.id, input.minigameId),
        with: { queueEntries: true },
      });

      if (minigameData === undefined) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      const playersInQueue = minigameData.queueEntries.length;

      if (playersInQueue + 1 >= minigameData.minPlayers) {
        const queuedPlayers = minigameData.queueEntries.map(
          (q) => q.playerUuid
        );

        return {
          action: "start_game",
          playerUuids: [input.playerUuid, ...queuedPlayers],
          minigameId: minigameData.id,
        };
      }

      return {
        action: "added",
        playersInQueue: playersInQueue + 1,
      };
    }),
  removePlayer: procedure
    .input(z.object({ playerUuid: z.string() }))
    .mutation(async ({ input }) => {
      await db
        .delete(queueTable)
        .where(eq(queueTable.playerUuid, input.playerUuid));
    }),
});
