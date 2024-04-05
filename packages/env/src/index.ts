import * as v from "valibot";

const envSchema = v.object({
  API_TOKEN_SECRET: v.string(),
  DATABASE_TOKEN: v.string(),
  DATABASE_URL: v.string(),
  JWT_PRIVATE_KEY: v.string(),
});

const env = v.parse(envSchema, process.env);

export { env };
