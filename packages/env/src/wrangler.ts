import z from "zod";

const envSchema = z.object({
  WRANGLER_PORT: z.coerce.number(),
  WRANGLER_HOST: z.string(),
});

const env = envSchema.parse(process.env);

export { env };
