import { eq } from "drizzle-orm";
import { db, Table } from "..";

export async function getKv<T>(key: string, defaultValue: T): Promise<T>;
export async function getKv<T>(key: string): Promise<T | null>;
export async function getKv<T>(key: string, defaultValue: T | null = null): Promise<T | null> {
  const result = await db.select().from(Table.kv).where(eq(Table.kv.key, key)).limit(1);

  return result[0].value ? JSON.parse(result[0].value) : defaultValue;
}

export async function setKv<T>(key: string, value: T): Promise<void> {
  await db
    .insert(Table.kv)
    .values({ key, value: JSON.stringify(value) })
    .onConflictDoUpdate({
      target: [Table.kv.key],
      set: {
        value: JSON.stringify(value),
      },
    });
}

export async function deleteKv(key: string): Promise<void> {
  await db.delete(Table.kv).where(eq(Table.kv.key, key));
}

export async function hasKv(key: string): Promise<boolean> {
  const result = await db.select().from(Table.kv).where(eq(Table.kv.key, key)).limit(1);

  return result.length > 0;
}
