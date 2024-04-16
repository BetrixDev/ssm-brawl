import z from "zod";

const envSchema = z.object({
  BOT_TOKEN: z.string(),
});

const env = envSchema.parse(process.env);

export { env };
