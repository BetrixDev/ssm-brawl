import { Column } from "typeorm";
import { HistoricalGameKit } from "./HistoricalGameKit.js";

export class HistoricalGamePlayer {
  @Column()
  uuid: string;

  @Column()
  stocksLeft: number;

  @Column(() => HistoricalGameKit)
  kitsUsed: HistoricalGameKit[];

  constructor(uuid: string, stocksLeft: number, kitsUsed: HistoricalGameKit[]) {
    this.uuid = uuid;
    this.stocksLeft = stocksLeft;
    this.kitsUsed = kitsUsed;
  }
}
