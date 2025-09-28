export function calculateDailyLoginStreak(
  previousStreak: number | null | undefined,
  lastJoinDate: Date | null | undefined,
  now: Date = new Date(),
): number {
  const previous = typeof previousStreak === "number" && previousStreak > 0 ? previousStreak : 0;

  if (!lastJoinDate) {
    return 1;
  }

  const currentDay = startOfDay(now);
  const lastJoinDay = startOfDay(lastJoinDate);

  const diffDays = Math.floor((currentDay.getTime() - lastJoinDay.getTime()) / MS_IN_DAY);

  if (diffDays <= 0) {
    return previous > 0 ? previous : 1;
  }

  if (diffDays === 1) {
    return previous + 1;
  }

  return 1;
}

const MS_IN_DAY = 86_400_000;

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}
