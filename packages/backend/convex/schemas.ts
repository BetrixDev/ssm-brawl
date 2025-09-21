import z from "zod/v3";

export const playerDocumentSchema = z.object({
  isFirstTimeOnServer: z.boolean(),
  avatarUrl: z.string(),
  lastJoinTime: z.string(),
  stats: z.record(z.string(), z.union([z.string(), z.number(), z.boolean()])),
});

export type PlayerDocument = z.infer<typeof playerDocumentSchema>;
