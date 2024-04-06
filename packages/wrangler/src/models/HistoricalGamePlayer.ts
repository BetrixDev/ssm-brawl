import { Column } from "typeorm";
import { HistoricalGamePlayerKit } from "./HistoricalGamePlayerKit";

export class HistoricalGamePlayer {
  @Column()
  uuid: string;

  @Column()
  stocksLeft: number;

  @Column(() => HistoricalGamePlayerKit)
  kitsUsed: HistoricalGamePlayerKit[];

  constructor(
    uuid: string,
    stocksLeft: number,
    kitsUsed: HistoricalGamePlayerKit[]
  ) {
    this.uuid = uuid;
    this.stocksLeft = stocksLeft;
    this.kitsUsed = kitsUsed;
  }
}
