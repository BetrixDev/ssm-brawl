import z from "zod";

const envSchema = z.object({
  API_TOKEN_SECRET: z.string(),
  API_PORT: z.coerce.number(),
  API_PROTOCOL: z.string(),
  API_HOST: z.string(),
  JWT_PRIVATE_KEY: z.string(),
  NODE_ENV: z.string().optional(),
});

const env = envSchema.parse(process.env);

export { env };
