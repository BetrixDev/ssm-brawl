import { config } from "dotenv";
import { z } from "zod";

config({ path: "./.env" });

const nodeEnvSchema = z.enum(["development", "production"]).default("development");

const baseEnvSchema = z.object({
  NODE_ENV: nodeEnvSchema,
});

const devSchema = baseEnvSchema.extend({
  DATABASE_URL: z.string().default("postgres://postgres:brawl@database:5432/postgres"),
  CORS_ORIGIN: z.string().optional().default("http://localhost:3001"),
  BETTER_AUTH_SECRET: z.string().default("changeme"),
  BETTER_AUTH_URL: z.string().url().default("http://localhost:3000"),
  POLAR_ACCESS_TOKEN: z.string().default(""),
  POLAR_SUCCESS_URL: z.string().url().default("http://localhost:3001/checkout/success"),
  PLUGIN_SECRET_KEY: z.string().default(""),
});

const prodSchema = baseEnvSchema.extend({
  DATABASE_URL: z.string(),
  CORS_ORIGIN: z.string().optional(),
  BETTER_AUTH_SECRET: z.string(),
  BETTER_AUTH_URL: z.string().url(),
  POLAR_ACCESS_TOKEN: z.string(),
  POLAR_SUCCESS_URL: z.string().url(),
  PLUGIN_SECRET_KEY: z.string(),
});

const envSchemas = {
  development: devSchema,
  production: prodSchema,
};

const nodeEnv = nodeEnvSchema.parse(process.env.NODE_ENV);

export const env = envSchemas[nodeEnv].parse(process.env);
