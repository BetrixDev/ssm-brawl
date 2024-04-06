import { Column } from "typeorm";

export class HistoricalGamePlayerKit {
  @Column()
  id: string;

  @Column()
  dateFirstUsed: number;

  @Column()
  dateLastUsed: number;

  constructor(id: string, dateFirstUsed: number, dateLastUsed: number) {
    this.id = id;
    this.dateFirstUsed = dateFirstUsed;
    this.dateLastUsed = dateLastUsed;
  }
}
