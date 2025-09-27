import type { PlayerDocument } from "@/schemas/player-document";
import z from "zod";

const number = z.number();

export function incrementStat(
  stats: PlayerDocument["stats"],
  key: string,
  defaultValue: number,
): number;
export function incrementStat(
  stats: PlayerDocument["stats"],
  key: string,
): number | string | boolean;
export function incrementStat(stats: PlayerDocument["stats"], key: string, defaultValue?: number) {
  try {
    return number.parse(stats[key]) + 1;
  } catch {
    if (defaultValue !== undefined) {
      return defaultValue;
    }

    return stats[key];
  }
}
