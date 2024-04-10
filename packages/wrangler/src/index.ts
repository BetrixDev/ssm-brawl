import "reflect-metadata";
import { DataSource } from "typeorm";
import { HistoricalGame } from "./entities/HistoricalGame.js";
import { env } from "env/wrangler";

export const WranglerDataSource = new DataSource({
  type: "mongodb",
  database: "ssmb",
  host: env.WRANGLER_HOST,
  port: env.WRANGLER_PORT,
  entities: [HistoricalGame],
});

export const wranglerClient = WranglerDataSource.mongoManager;
