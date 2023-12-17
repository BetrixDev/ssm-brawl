import type { Config } from "drizzle-kit";
import { env } from "./src/env";

export default {
  schema: "./src/db/schemas/*.ts",
  driver: "mysql2",
  dbCredentials: {
    uri: env.DB_CONNECTION_STRING,
  },
} satisfies Config;
