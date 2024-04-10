import z from "zod";

const envSchema = z.object({
  TUSSLER_TOKEN: z.string(),
  TUSSLER_URL: z.string(),
});

const env = envSchema.parse(process.env);

export { env };
