import type { Config } from "drizzle-kit";
import { env } from "./src/env";

export default {
  schema: "./src/db/schemas/*.ts",
  driver: "pg",
  dbCredentials: { connectionString: env.DB_CONNECTION_STRING },
} satisfies Config;
