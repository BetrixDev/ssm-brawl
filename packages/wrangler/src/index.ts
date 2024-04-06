import "reflect-metadata";
import { DataSource } from "typeorm";
import { HistoricalGame } from "./entities/HistoricalGame.js";
import { env } from "env";

export const WranglerDataSource = new DataSource({
  type: "mongodb",
  database: "ssmb",
  host: env.WRANGLER_HOST,
  port: env.WRANGLER_PORT,
  entities: [HistoricalGame],
});

export const wranglerClient = WranglerDataSource.mongoManager;

export * from "./entities/HistoricalGame.js";
export * from "./models/HistoricalGamePlayer.js";
export * from "./models/HistoricalGameKit.js";
export * from "./models/HistoricalGameKitAbilityUse.js";
