import { Column, Entity, ObjectId, ObjectIdColumn } from "typeorm";
import { HistoricalGamePlayer } from "../models/HistoricalGamePlayer.js";

@Entity()
export class HistoricalGame {
  @ObjectIdColumn()
  gameId: string;

  @Column()
  minigameId: string;

  @Column()
  mapId: string;

  @Column(() => HistoricalGamePlayer)
  players: HistoricalGamePlayer[];

  constructor(
    gameId: string,
    minigameId: string,
    mapId: string,
    players: HistoricalGamePlayer[]
  ) {
    this.gameId = gameId;
    this.minigameId = minigameId;
    this.mapId = mapId;
    this.players = players;
  }
}
