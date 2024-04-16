import z from "zod";

const envSchema = z.object({
  TUSSLER_TOKEN: z.string(),
  TUSSLER_URL: z.string(),
  TUSSLER_SYNC_URL: z.string(),
  TUSSLER_SYNC_INTERVAL: z.coerce.number()
});

const env = envSchema.parse(process.env);

export { env };
