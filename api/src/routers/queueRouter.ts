import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { db, queue, eq, minigames, inArray } from "tussler";
import { TRPCError } from "@trpc/server";
import { kv } from "../kv.js";
import { useRandomId } from "../utils.js";

export const queueRouter = router({
  addPlayer: internalProcedure
    .input(
      z.object({
        playerUuid: z.string(),
        minigameId: z.string(),
        force: z.boolean().optional(),
      }),
    )
    .mutation(async ({ input }) => {
      const isServerShuttingDown = await kv.getItem("isShuttingDown");

      if (isServerShuttingDown) {
        throw new TRPCError({ code: "CONFLICT", message: "serverShuttingDown" });
      }

      const minigameData = await db.query.minigames.findFirst({
        where: eq(minigames.id, input.minigameId),
        with: { queueEntries: true },
      });

      if (minigameData === undefined) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      if (input.force) {
        await db.delete(queue).where(eq(queue.playerUuid, input.playerUuid));
      } else {
        const playerInQueue = await db.query.queue.findFirst({
          where: eq(queue.playerUuid, input.playerUuid),
        });

        if (playerInQueue !== undefined) {
          throw new TRPCError({ code: "CONFLICT", message: "alreadyInQueue" });
        }
      }

      const playerParty = await db.query.partyGuests.findFirst({
        where: (table, { eq }) => eq(table.playerUuid, input.playerUuid),
        with: { party: { with: { guests: true } } },
      });

      if (playerParty !== undefined) {
        const playersInParty = playerParty.party.guests.map((p) => p.playerUuid);

        if (playersInParty.length !== minigameData.playersPerTeam) {
          throw new TRPCError({
            code: "CONFLICT",
            message: "partyCountMismatch",
          });
        }

        const playersInPartyInQueue = await db.query.queue.findMany({
          where: (table, { inArray }) => inArray(table.playerUuid, playersInParty),
        });

        if (playersInPartyInQueue.length > 0) {
          throw new TRPCError({
            code: "CONFLICT",
            message: "playersInPartyInQueue",
          });
        }

        await db.insert(queue).values(
          playersInParty.map((playerUuid) => ({
            dateAdded: Date.now(),
            minigameId: minigameData.id,
            playerUuid: playerUuid,
            partyId: playerParty.partyId,
          })),
        );
      } else {
        await db.insert(queue).values({
          dateAdded: Date.now(),
          minigameId: minigameData.id,
          playerUuid: input.playerUuid,
        });
      }

      const playersInQueue = await db.query.queue.findMany({
        where: (table, { eq }) => eq(table.minigameId, minigameData.id),
        orderBy: (table, { asc }) => [asc(table.dateAdded)],
      });

      const isTeamMinigame = minigameData.playersPerTeam !== 1;

      if (isTeamMinigame) {
        if (playersInQueue.length > minigameData.playersPerTeam * minigameData.amountOfTeams) {
          const teams: { id: string; players: string[] }[] = [];

          for (let i = 0; i < minigameData.amountOfTeams; i += minigameData.playersPerTeam) {
            const teamSlice = playersInQueue.slice(i, minigameData.playersPerTeam * (i + 1));

            teams.push({
              id: useRandomId(10),
              players: teamSlice.map(({ playerUuid }) => playerUuid),
            });
          }

          return {
            type: "start_game",
            teams: teams,
            minigameId: minigameData.id,
          };
        } else {
          return {
            type: "added",
            playersInQueue: playersInQueue.length,
          };
        }
      } else {
        if (playersInQueue.length >= minigameData.minPlayers) {
          const teams = playersInQueue
            .slice(0, minigameData.maxPlayers)
            .map((p) => ({ id: useRandomId(10), players: [p.playerUuid] }));

          return {
            type: "start_game",
            teams: teams,
            minigameId: minigameData.id,
          };
        } else {
          return {
            type: "added",
            playersInQueue: playersInQueue.length,
          };
        }
      }
    }),
  removePlayers: internalProcedure
    .input(z.object({ playerUuids: z.array(z.string()) }))
    .mutation(async ({ input }) => {
      await db.delete(queue).where(inArray(queue.playerUuid, input.playerUuids)).execute();
    }),
  flushQueue: internalProcedure.mutation(async () => {
    await db.delete(queue).execute();
  }),
});
