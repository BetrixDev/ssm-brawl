import { z } from "zod";
import { and, db, eq, friendships, or } from "tussler";
import { internalProcedure, router } from "../trpc.js";

export const friendshipRouter = router({
  canAddAsFriend: internalProcedure
    .input(
      z.object({
        inviterUuid: z.string(),
        inviteeUuid: z.string(),
      }),
    )
    .query(async ({ input }) => {
      const existingFriendship = await db.query.friendships.findFirst({
        where: (table, { eq, and }) =>
          and(eq(table.uuid1, input.inviterUuid), eq(table.uuid2, input.inviteeUuid)),
      });

      if (existingFriendship !== undefined) {
        return {
          success: false,
          lang: "gui.friendships.alreadyfriends",
        };
      }

      const inviteePlayerData = await db.query.basicPlayerData.findFirst({
        where: (table, { eq }) => eq(table.uuid, input.inviteeUuid),
      });

      if (inviteePlayerData && inviteePlayerData.areFriendRequestsOff) {
        return {
          success: false,
          lang: "gui.friendships.cantrequesttoplayer",
        };
      }

      return {
        success: true,
      };
    }),
  createFriendship: internalProcedure
    .input(
      z.object({
        inviterUuid: z.string(),
        inviteeUuid: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      await db
        .insert(friendships)
        .values([
          {
            uuid1: input.inviterUuid,
            uuid2: input.inviteeUuid,
          },
          {
            uuid1: input.inviteeUuid,
            uuid2: input.inviterUuid,
          },
        ])
        .execute();
    }),
  removeFriendship: internalProcedure
    .input(
      z.object({
        player1Uuid: z.string(),
        player2Uuid: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      await db
        .delete(friendships)
        .where(
          or(
            and(eq(friendships.uuid1, input.player1Uuid), eq(friendships.uuid2, input.player2Uuid)),
            and(eq(friendships.uuid1, input.player2Uuid), eq(friendships.uuid2, input.player1Uuid)),
          ),
        );
    }),
  getPlayersFriends: internalProcedure
    .input(
      z.object({
        playerUuid: z.string(),
      }),
    )
    .query(async ({ input }) => {
      return await db.query.friendships.findMany({
        where: (table, { eq }) => eq(table.uuid1, input.playerUuid),
      });
    }),
  getPlayersFriendsFromList: internalProcedure
    .meta({
      description:
        "Useful for when a player joins the server and you want to know if they have any friends currently online",
    })
    .input(
      z.object({
        playerUuid: z.string().describe("Player to check for friendships"),
        otherPlayers: z.array(z.string()).describe("Array of players to check against"),
      }),
    )
    .query(async ({ input }) => {
      const tusslerResponse = await db.query.friendships.findMany({
        where: (table, { inArray }) => inArray(table.uuid1, input.otherPlayers),
      });

      return tusslerResponse.map(({ uuid1 }) => uuid1);
    }),
});
