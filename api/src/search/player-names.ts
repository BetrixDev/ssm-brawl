import MiniSearch, { type SearchOptions } from "minisearch";
import { db } from "tussler";

const SEARCH_FIELDS = ["username", "uuid"] as const;

type SearchFields = { [key in (typeof SEARCH_FIELDS)[number]]: string };

const playerNameEngine = new MiniSearch<SearchFields>({
  fields: ["username", "uuid"],
});

export async function initPlayerNameEngine() {
  const userCacheEntries = await db.query.userCache.findMany({});

  playerNameEngine.addAll(userCacheEntries);
}

export function addPlayer(uuid: string, username: string) {
  playerNameEngine.add({ uuid, username });
}

export function removePlayer(uuid: string, username: string) {
  playerNameEngine.remove({ uuid, username });
}

export function searchPlayerNames(
  query: string,
  opts?: SearchOptions & { limit?: number }
) {
  const result = playerNameEngine.search(query, opts) as any as SearchFields[];

  return result
    .map((res) => ({
      name: res.username,
      value: res.uuid,
    }))
    .slice(0, (opts?.limit ?? 25) - 1);
}
