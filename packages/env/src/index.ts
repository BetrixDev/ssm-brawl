import * as v from "valibot";
import "dotenv/config";

const envSchema = v.object({
  DATABASE_HOST: v.string(),
  DATABASE_USERNAME: v.string(),
  DATABASE_PASSWORD: v.string(),
  UPSTASH_KAFKA_REST_URL: v.string(),
  UPSTASH_KAFKA_REST_USERNAME: v.string(),
  UPSTASH_KAFKA_REST_PASSWORD: v.string(),
});

const env = v.parse(envSchema, process.env);

export { env };
