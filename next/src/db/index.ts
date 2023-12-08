import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import { env } from "@/env";

const client = postgres(env.DB_CONNECTION_STRING!);
export const db = drizzle(client);

export * from "drizzle-orm";
export * from "./schemas";
