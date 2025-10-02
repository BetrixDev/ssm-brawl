import { config } from "dotenv";
import { z } from "zod";

config({ path: "./.env" });

const envSchema = z.object({
  CORS_ORIGIN: z.string().optional(),
  BETTER_AUTH_SECRET: z.string(),
  BETTER_AUTH_URL: z.url(),
  POLAR_ACCESS_TOKEN: z.string(),
  POLAR_SUCCESS_URL: z.url(),
  POLAR_SERVER: z.enum(["production", "sandbox"]).default("sandbox"),
  PLUGIN_SECRET_KEY: z.string(),
});

export const env = envSchema.parse(process.env);
