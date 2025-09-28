import { z } from "zod";

export const playerDocumentSchema = z.object({
  isFirstTimeOnServer: z.boolean(),
  lastJoinDate: z.string(),
  headSkinBase64: z.string().nullable(),
  dailyLoginStreak: z.number().int().positive().nullable(),
  stats: z.record(z.string(), z.union([z.string(), z.number(), z.boolean()])),
  banData: z
    .array(
      z.object({
        id: z.string(),
        isBanned: z.boolean(),
        reason: z.string(),
        expiresAt: z.string().nullable(),
        bannedAt: z.string(),
        bannedBy: z.string(),
      }),
    )
    .nullable(),
});

export type PlayerDocument = z.infer<typeof playerDocumentSchema>;
