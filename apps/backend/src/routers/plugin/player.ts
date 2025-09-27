import { getPlayerDocument, updatePlayerDocument } from "@/db/queries/player-documents";
import { dbProvider, pluginProcedure } from "@/lib/orpc";
import { playerDocumentSchema } from "@/schemas/player-document";
import * as z from "zod";

export const pluginPlayersRouter = {
  getPlayerDocument: pluginProcedure
    .route({ method: "GET", path: "/{uuid}/document" })
    .output(playerDocumentSchema)
    .input(
      z.object({
        uuid: z.string(),
      }),
    )
    .use(dbProvider)
    .handler(async ({context, input }) => {
      return await getPlayerDocument(context.db, input.uuid);
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
