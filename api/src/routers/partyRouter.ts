import { z } from "zod";
import { internalProcedure, router } from "../trpc.js";
import { db, eq, parties, partyGuests, partyInvites } from "tussler";
import { TRPCError } from "@trpc/server";
import { useRandomId } from "../utils.js";

export const partyRouter = router({
  createParty: internalProcedure
    .input(
      z.object({
        ownerUuid: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      const existingPartiesPlayerIsIn = await db.query.partyGuests.findFirst({
        where: (table, { eq }) => eq(table.playerUuid, input.ownerUuid),
      });

      if (existingPartiesPlayerIsIn) {
        throw new TRPCError({
          code: "CONFLICT",
        });
      }

      const partyId = useRandomId(15);

      await db.batch([
        db.insert(parties).values({
          ownerUuid: input.ownerUuid,
          partyId: partyId,
        }),
        db.insert(partyGuests).values({ partyId, playerUuid: input.ownerUuid }),
      ]);
    }),
  invitePlayerToParty: internalProcedure
    .input(
      z.object({
        inviteeUuid: z.string(),
        inviterUuid: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      const inviterParty = await db.query.partyGuests.findFirst({
        where: (table, { eq }) => eq(table.playerUuid, input.inviterUuid),
        with: { party: true },
      });

      if (!inviterParty) {
        throw new TRPCError({
          code: "BAD_REQUEST",
          message: "notInParty",
        });
      }

      const inviteeParty = await db.query.partyGuests.findFirst({
        where: (table, { eq }) => eq(table.playerUuid, input.inviteeUuid),
      });

      if (inviteeParty) {
        throw new TRPCError({
          code: "CONFLICT",
          message: "inviteeInParty",
        });
      }

      if (inviterParty.party.ownerUuid !== input.inviterUuid) {
        throw new TRPCError({
          code: "BAD_REQUEST",
          message: "notOwner",
        });
      }

      const existingInvite = await db.query.partyInvites.findFirst({
        where: (table, { eq, and }) =>
          and(eq(table.inviteeUuid, input.inviteeUuid), eq(table.partyId, inviterParty.partyId)),
      });

      if (existingInvite) {
        throw new TRPCError({
          code: "CONFLICT",
          message: "inviteePendingInvite",
        });
      }

      await db.insert(partyInvites).values({
        partyId: inviterParty.partyId,
        inviteeUuid: input.inviteeUuid,
        inviterUuid: input.inviterUuid,
      });
    }),
  acceptInvite: internalProcedure
    .input(
      z.object({
        inviteeUuid: z.string(),
        inviterUuid: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      const allPlayerInvites = await db.query.partyInvites.findFirst({
        where: (table, { eq, and }) =>
          and(eq(table.inviteeUuid, input.inviteeUuid), eq(table.inviterUuid, input.inviterUuid)),
      });

      if (!allPlayerInvites) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }
    }),
  leaveParty: internalProcedure
    .input(z.object({ playerUuid: z.string() }))
    .mutation(async ({ input }) => {
      const playerParty = await db.query.partyGuests.findFirst({
        where: (table, { eq }) => eq(table.playerUuid, input.playerUuid),
        with: { party: true },
      });

      if (!playerParty) {
        throw new TRPCError({ code: "BAD_REQUEST" });
      }

      if (playerParty.party.ownerUuid === input.playerUuid) {
        await db.batch([
          db.delete(partyGuests).where(eq(partyGuests.partyId, playerParty.partyId)),
          db.delete(parties).where(eq(parties.partyId, playerParty.partyId)),
        ]);

        return { type: "disband" };
      } else {
        await db.delete(partyGuests).where(eq(partyGuests.playerUuid, input.playerUuid)).execute();

        return { type: "left", playerUuid: input.playerUuid };
      }
    }),
});
