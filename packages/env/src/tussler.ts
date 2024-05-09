import z from "zod";

const envSchema = z.object({
  TUSSLER_TOKEN: z.string(),
  TUSSLER_URL: z.string(),
  TUSSLER_SYNC_URL: z.string().optional(),
  TUSSLER_SYNC_INTERVAL: z.coerce.number().optional(),
  NODE_ENV: z.string().optional(),
});

const env = envSchema.parse(process.env);

export { env };
