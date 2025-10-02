import type { PaginationOptions } from "@/schemas/pagination-options";
import { sql } from "drizzle-orm";
import { db, Table } from "..";

export function searchPlayerUsernames(query: string, paginationOptions: PaginationOptions) {
  return db
    .select()
    .from(Table.players)
    .where(
      sql`lower(${Table.players.username}) LIKE lower(${'%' + query + '%'})`,
    )
    .orderBy(
      sql`CASE
        WHEN lower(${Table.players.username}) = lower(${query}) THEN 0
        WHEN lower(${Table.players.username}) LIKE lower(${query + '%'}) THEN 1
        WHEN instr(lower(${Table.players.username}), lower(${query})) > 0 THEN 2
        ELSE 3
      END ASC,
      instr(lower(${Table.players.username}), lower(${query})) ASC,
      length(${Table.players.username}) ASC,
      ${Table.players.username} ASC`,
    )
    .limit(paginationOptions.limit)
    .offset((paginationOptions.page - 1) * paginationOptions.limit);
}
