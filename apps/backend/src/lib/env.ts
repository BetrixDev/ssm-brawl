import { config } from "dotenv";
import { z } from "zod";

config({ path: "./.env" });

const baseEnvSchema = z.object({
  NODE_ENV: z.enum(["development", "production", "test"]).default("development"),
});

const baseEnv = baseEnvSchema.parse(process.env);

const isDev = baseEnv.NODE_ENV === "development";

const envSchema = isDev
  ? baseEnvSchema.extend({
      DATABASE_URL: z.string().default("./data/database.sqlite"),
      NODE_ENV: z.enum(["development", "production", "test"]).default("development"),
      CORS_ORIGIN: z.string().optional().default("http://localhost:3001"),
      BETTER_AUTH_SECRET: z.string().default("changeme"),
      BETTER_AUTH_URL: z.url().default("http://localhost:3000"),
      POLAR_ACCESS_TOKEN: z.string().default(""),
      POLAR_SUCCESS_URL: z.url().default("http://localhost:3001/checkout/success"),
      PLUGIN_SECRET_KEY: z.string().default(""),
    })
  : baseEnvSchema.extend({
      DATABASE_URL: z.string(),
      NODE_ENV: z.enum(["development", "production", "test"]).default("development"),
      CORS_ORIGIN: z.string().optional(),
      BETTER_AUTH_SECRET: z.string(),
      BETTER_AUTH_URL: z.url(),
      POLAR_ACCESS_TOKEN: z.string(),
      POLAR_SUCCESS_URL: z.url(),
      PLUGIN_SECRET_KEY: z.string(),
    });

export const env = envSchema.parse(process.env);
