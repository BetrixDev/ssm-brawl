import { getPlayerDocument, handlePlayerJoinEvent, updatePlayerDocument } from "@/db/queries/player-documents";
import { incrementStat } from "@/helpers/stats-helpers";
import { dbProvider, pluginProcedure } from "@/lib/orpc";
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
    .use(dbProvider)
    .handler(async ({context, input }) => {
      const document = await getPlayerDocument(context.db, input.params.uuid);


      if (input.query.joinEvent) {
        await handlePlayerJoinEvent(context.db, input.params.uuid);
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
    .use(dbProvider)
    .handler(async ({context, input }) => {
      await updatePlayerDocument(context.db, input.uuid, input.document);

      return {
        message: "Player document saved",
      };
    }),
};
