import { internalProcedure, router } from "../trpc.js";
import { z } from "zod";
import { db, messageChannels, messages, messageViewers } from "tussler";
import { TRPCError } from "@trpc/server";
import { sortStrings, useRandomId } from "../utils.js";

export const pmRouter = router({
  messagePlayer: internalProcedure
    .input(
      z.object({
        authorUuid: z.string(),
        targetUuid: z.string(),
        content: z.string(),
      }),
    )
    .mutation(async ({ input }) => {
      let canSendMessage = false;

      const friendship = await db.query.friendships.findFirst({
        where: (table, { eq, and }) =>
          and(eq(table.uuid1, input.authorUuid), eq(table.uuid2, input.targetUuid)),
      });

      if (friendship !== undefined) {
        canSendMessage = true;
      } else {
        const targetPlayerData = await db.query.basicPlayerData.findFirst({
          where: (table, { eq }) => eq(table.uuid, input.targetUuid),
        });

        if (targetPlayerData && targetPlayerData.canReceiveRandomMessages) {
          canSendMessage = true;
        }
      }

      if (!canSendMessage) {
        throw new TRPCError({ code: "FORBIDDEN" });
      }

      const sortedUuids = sortStrings([input.authorUuid, input.targetUuid]);
      const channelId = `pm-${sortedUuids.join("-")}`;
      const messageId = useRandomId(15);

      const existingChannel = await db.query.messageChannels.findFirst({
        where: (table, { eq }) => eq(table.id, channelId),
      });

      if (!existingChannel) {
        await db.batch([
          db.insert(messageChannels).values({
            id: channelId,
          }),
          db.insert(messageViewers).values([
            {
              channelId,
              playerUuid: input.authorUuid,
            },
            {
              channelId,
              playerUuid: input.targetUuid,
            },
          ]),
        ]);
      }

      await db.insert(messages).values({
        id: messageId,
        content: input.content,
        channelId: channelId,
        authorUuid: input.authorUuid,
        time: Date.now(),
      });

      return {
        success: true,
      };
    }),
  getMessageHistory: internalProcedure
    .input(
      z.object({
        playerUuids: z.array(z.string()),
      }),
    )
    .query(async ({ input }) => {
      const sortedUuids = sortStrings(input.playerUuids);
      const channelId = `pm-${sortedUuids.join("-")}`;

      const messageChannel = await db.query.messageChannels.findFirst({
        where: (table, { eq }) => eq(table.id, channelId),
        with: { messages: true },
      });

      if (!messageChannel) {
        return [];
      }

      return {
        messages: messageChannel.messages,
      };
    }),
});
