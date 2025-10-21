import { MutationCtx, QueryCtx } from "../_generated/server";

type KvKey = "online_player_count" | (string & {});

export async function setKv(ctx: MutationCtx, key: KvKey, value: any) {
  const existingKv = await ctx.db
    .query("kv")
    .withIndex("by_key", (q) => q.eq("key", key))
    .first();

  if (existingKv) {
    await ctx.db.patch(existingKv._id, {
      value,
    });
  } else {
    await ctx.db.insert("kv", {
      key,
      value,
    });
  }
}

export async function getKv<T>(ctx: QueryCtx, key: KvKey): Promise<T | undefined>;
export async function getKv<T>(ctx: QueryCtx, key: KvKey, defaultValue: T): Promise<T>;
export async function getKv<T>(
  ctx: QueryCtx,
  key: KvKey,
  defaultValue?: T,
): Promise<T | undefined> {
  const kv = await ctx.db
    .query("kv")
    .withIndex("by_key", (q) => q.eq("key", key))
    .first();

  if (!kv) {
    return defaultValue;
  }

  return kv.value as T;
}

export async function deleteKv(ctx: MutationCtx, key: KvKey) {
  const existingKv = await ctx.db
    .query("kv")
    .withIndex("by_key", (q) => q.eq("key", key))
    .first();

  if (existingKv) {
    await ctx.db.delete(existingKv._id);
  }
}
