import * as v from "valibot";

const envSchema = v.object({
  API_AUTH_TOKEN: v.string(),
  DATABASE_HOST: v.string(),
  DATABASE_USERNAME: v.string(),
  DATABASE_PASSWORD: v.string(),
  DATABASE_URI: v.string(),
});

const env = v.parse(envSchema, process.env);

export { env };
