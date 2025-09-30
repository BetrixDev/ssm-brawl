import { RedisClient } from "bun";
import { env } from "./env";

export function getRedis() {
  return new RedisClient(env.REDIS_URL);
}
