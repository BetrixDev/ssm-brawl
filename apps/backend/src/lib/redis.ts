import { createClient } from "redis";
import { env } from "./env";

export async function getRedis() {
  const redis = createClient({
    url: env.REDIS_URL,
  });

  await redis.connect();

  return redis;
}
