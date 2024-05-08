import { internalProcedure, router } from "../trpc.js";
import { z } from "zod";
import { db } from "tussler";
import { wranglerClient } from "wrangler";
import { MessageChannel } from "wrangler/entities/MessageChannel.js";
import { MessageChannelMessage } from "wrangler/models/MessageChannelMessage.js";

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
        return {
          success: false,
          message: "gui.pm.error.cannotSendMessage",
        };
      }

      const messageChannelRepo = wranglerClient.getMongoRepository(MessageChannel);

      const messageEntry = new MessageChannelMessage(input.authorUuid, input.content);

      const existingChannel = await messageChannelRepo.findOne({
        where: {
          users: {
            $all: [input.authorUuid, input.targetUuid],
          },
        },
      });

      if (!existingChannel) {
        await messageChannelRepo.insertOne(
          new MessageChannel([input.authorUuid, input.targetUuid], [messageEntry]),
        );
      } else {
        existingChannel.messages.push(messageEntry);
        existingChannel.messages = existingChannel.messages.slice(0, 24);

        await messageChannelRepo.save(existingChannel);
      }

      return {
        success: true,
      };
    }),
  getMessageHistory: internalProcedure
    .input(
      z.object({
        users: z.array(z.string()),
      }),
    )
    .query(async ({ input }) => {
      const messageChannel = await wranglerClient.getMongoRepository(MessageChannel).findOne({
        where: {
          users: {
            $all: input.users,
          },
        },
      });

      if (!messageChannel) {
        return {
          success: false,
          message: "gui.pm.error.noMessageHistory",
        };
      }

      return {
        messages: messageChannel.messages,
      };
    }),
});
