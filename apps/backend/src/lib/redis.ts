import { RedisClient } from "bun";
import { env } from "./env";

export async function getRedis() {
  return new RedisClient(env.REDIS_URL);
}
