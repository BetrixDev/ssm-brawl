import z from "zod/v3";

export const playerDocumentSchema = z.object({
  isFirstTimeOnServer: z.boolean(),
  avatarUrl: z.string(),
  lastJoinDate: z.string(),
  stats: z.record(z.string(), z.union([z.string(), z.number(), z.boolean()])),
  banData: z.nullable(
    z.object({
      isBanned: z.boolean(),
      reason: z.string(),
      expiresAt: z.optional(z.string()),
      bannedAt: z.string(),
      bannedBy: z.string(),
    }),
  ),
});

export type PlayerDocument = z.infer<typeof playerDocumentSchema>;
