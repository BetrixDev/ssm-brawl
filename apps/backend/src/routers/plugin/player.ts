import { getPlayerDocument, updatePlayerDocument } from "@/db/queries/player-documents";
import { pluginProcedure } from "@/lib/orpc";
import { playerDocumentSchema } from "@/schemas/player-document";
import * as z from "zod";

export const pluginPlayersRouter = {
  getPlayerDocument: pluginProcedure
    .route({ method: "GET", path: "/player/{uuid}/document" })
    .output(playerDocumentSchema)
    .input(
      z.object({
        uuid: z.string(),
      }),
    )
    .handler(async ({ input }) => {
      return await getPlayerDocument(input.uuid);
    }),
  savePlayerDocument: pluginProcedure
    .route({ method: "PUT", path: "/player/{uuid}/document" })
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
