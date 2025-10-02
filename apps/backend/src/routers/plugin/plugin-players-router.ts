import { getPlayerDocument, handlePlayerJoinEvent, updatePlayerDocument } from "@/db/queries/player-documents";
import {  pluginProcedure } from "@/lib/orpc";
import { playerDocumentSchema } from "@/schemas/player-document";
import * as z from "zod";

export const pluginPlayersRouter = {
  getPlayerDocument: pluginProcedure
    .route({ method: "GET", path: "/{uuid}/document", inputStructure: "detailed" })
    .output(playerDocumentSchema)
    .input(
     z.object({
        params:  z.object({
          uuid: z.string(),
        }),
        query: z.object({
          joinEvent: z.stringbool().default(false),
        }),
     })
    )
    .handler(async ({ input }) => {
      const document = await getPlayerDocument( input.params.uuid);


      if (input.query.joinEvent) {
        await handlePlayerJoinEvent( input.params.uuid);
      } 

      return document
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
      await updatePlayerDocument( input.uuid, input.document);

      return {
        message: "Player document saved",
      };
    }),
};
