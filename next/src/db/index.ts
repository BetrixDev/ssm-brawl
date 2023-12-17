import { drizzle } from "drizzle-orm/planetscale-serverless";
import { connect } from "@planetscale/database";
import { env } from "@/env";

const client = connect({
  host: env.DATABASE_HOST,
  username: env.DATABASE_USERNAME,
  password: env.DATABASE_PASSWORD,
});
export const db = drizzle(client);

export * from "drizzle-orm";
export * from "./schemas";
