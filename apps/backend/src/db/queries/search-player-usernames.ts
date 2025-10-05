import { sql } from "drizzle-orm";
import { db, Table } from "../db";

export function searchPlayerUsernames(query: string) {
  return db
    .select({
      username: Table.players.username,
      uuid: Table.players.uuid,
    })
    .from(Table.players)
    .where(
      sql`to_tsvector('english', ${Table.players.username}) @@ to_tsquery('english', ${query})`,
    )
    .orderBy(
      sql`ts_rank(to_tsvector('english', ${Table.players.username}), to_tsquery('english', ${query})) DESC`,
    )
    .limit(25)
    .execute();
}
