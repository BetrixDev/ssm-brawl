import { z } from "zod";

export const paginationOptionsSchema = z.object({
  page: z.number().min(1),
  limit: z.number().min(1),
});

export type PaginationOptions = z.infer<typeof paginationOptionsSchema>;
