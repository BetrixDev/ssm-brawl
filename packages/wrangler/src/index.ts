import "reflect-metadata";
import { DataSource } from "typeorm";
import { HistoricalGame } from "./entities/HistoricalGame";

export const WranglerDataSource = new DataSource({
  type: "mongodb",
  database: "ssmb",
  host: "localhost",
  port: 27017,
  entities: [HistoricalGame],
});

export const wranglerClient = WranglerDataSource.mongoManager;

export * from "./entities/HistoricalGame";
export * from "./models/HistoricalGamePlayer";
export * from "./models/HistoricalGameKit";
export * from "./models/HistoricalGameKitAbilityUse";
