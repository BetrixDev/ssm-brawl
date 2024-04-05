import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { db, queueTable, eq, minigamesTable, inArray } from "../db/db.js";
import { TRPCError } from "@trpc/server";

export const queueRouter = router({
  addPlayer: internalProcedure
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

      if (minigameData.playersPerTeam % (playersInQueue + 1) !== 0) {
      }

      const isValidGroupSizeForQueue =
        minigameData.playersPerTeam % (playersInQueue + 1) !== 0;
      const isEnoughPlayersInQueue =
        playersInQueue + 1 >= minigameData.minPlayers;

      if (isValidGroupSizeForQueue && isEnoughPlayersInQueue) {
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
  removePlayers: internalProcedure
    .input(z.object({ playerUuids: z.array(z.string()) }))
    .mutation(async ({ input }) => {
      await db
        .delete(queueTable)
        .where(inArray(queueTable.playerUuid, input.playerUuids));
    }),
});
