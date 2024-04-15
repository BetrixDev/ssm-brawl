import { Column } from "typeorm";

export class HistoricalGameKitAbilityUse {
  @Column()
  abilityId: string;

  @Column()
  usedAt: number;

  @Column()
  damageDealt?: number;

  constructor(
    abilityId: string,
    usedAt: number,
    { damageDealt }: { damageDealt?: number } = {},
  ) {
    this.abilityId = abilityId;
    this.usedAt = usedAt;
    this.damageDealt = damageDealt;
  }
}
