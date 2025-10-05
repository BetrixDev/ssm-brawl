import { getPlayerDocument, updatePlayerDocument } from "@/db/queries/player-documents";
import {
  getOnlinePlayers,
  handlePlayerJoinEvent,
  handlePlayerQuitEvent,
} from "@/db/queries/players";
import { eventPublisher } from "@/lib/event-publisher";
import { pluginProcedure } from "@/lib/orpc";
import { playerDocumentSchema } from "@/schemas/player-document";
import * as z from "zod";

export const pluginPlayersRouter = {
  onPlayerJoin: pluginProcedure
    .route({ method: "POST", path: "/{uuid}/join", inputStructure: "detailed" })
    .input(
      z.object({
        params: z.object({
          uuid: z.string(),
        }),
      }),
    )
    .handler(async ({ input }) => {
      await handlePlayerJoinEvent(input.params.uuid);

      const onlinePlayers = await getOnlinePlayers();

      eventPublisher.publish("updatePlayerCount", {
        playerCount: onlinePlayers.length,
      });

      return {
        message: "Recorded player join event",
      };
    }),
  onPlayerQuit: pluginProcedure
    .route({ method: "POST", path: "/{uuid}/quit", inputStructure: "detailed" })
    .input(
      z.object({
        params: z.object({
          uuid: z.string(),
        }),
      }),
    )
    .handler(async ({ input }) => {
      await handlePlayerQuitEvent(input.params.uuid);
    }),
  getPlayerDocument: pluginProcedure
    .route({ method: "GET", path: "/{uuid}/document", inputStructure: "detailed" })
    .output(playerDocumentSchema)
    .input(
      z.object({
        params: z.object({
          uuid: z.string(),
        }),
      }),
    )
    .handler(async ({ input }) => {
      return await getPlayerDocument(input.params.uuid);
    }),
  savePlayerDocument: pluginProcedure
    .route({ method: "PUT", path: "/{uuid}/document" })
    .output(
      z.object({
        message: z.string(),
      }),
    )
    .input(
      z.object({
        uuid: z.string(),
        document: playerDocumentSchema,
      }),
    )
    .handler(async ({ input }) => {
      await updatePlayerDocument(input.uuid, input.document);

      return {
        message: "Player document saved",
      };
    }),
};
