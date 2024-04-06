import { Column } from "typeorm";
import { HistoricalGameKitAbilityUse } from "./HistoricalGameKitAbilityUse";

export class HistoricalGameKit {
  @Column()
  id: string;

  @Column()
  dateFirstUsed: number;

  @Column()
  dateLastUsed: number;

  @Column(() => HistoricalGameKitAbilityUse)
  abilityUsage: HistoricalGameKitAbilityUse[];

  constructor(
    id: string,
    dateFirstUsed: number,
    dateLastUsed: number,
    abilityUsage: HistoricalGameKitAbilityUse[] = []
  ) {
    this.id = id;
    this.dateFirstUsed = dateFirstUsed;
    this.dateLastUsed = dateLastUsed;
    this.abilityUsage = abilityUsage;
  }
}
