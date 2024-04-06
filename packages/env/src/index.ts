import * as v from "valibot";

const envSchema = v.object({
  API_TOKEN_SECRET: v.string(),
  TUSSLER_TOKEN: v.string(),
  TUSSLER_URL: v.string(),
  JWT_PRIVATE_KEY: v.string(),
  WRANGLER_HOST: v.string(),
  WRANGLER_PORT: v.coerce(v.number(), Number),
});

const env = v.parse(envSchema, process.env);

export { env };
