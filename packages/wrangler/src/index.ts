import "reflect-metadata";
import { DataSource } from "typeorm";
import { HistoricalGame } from "./entities/HistoricalGame.js";
import { env } from "env/wrangler";
import { MessageChannel } from "./entities/MessageChannel.js";

export const wranglerDataSource = new DataSource({
  type: "mongodb",
  database: "ssmb",
  host: env.WRANGLER_HOST,
  port: env.WRANGLER_PORT,
  entities: [HistoricalGame, MessageChannel],
});

export const wranglerClient = wranglerDataSource.mongoManager;
