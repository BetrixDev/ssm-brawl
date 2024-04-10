import z from "zod";

const envSchema = z.object({
  GITHUB_TOKEN: z.string(),
  WEBHOOK_SECRET: z.string(),
  NODE_ENV: z.string().default("development"),
  ORCHESTRATOR_PORT: z.coerce.number(),
});

const env = envSchema.parse(process.env);

export { env };
