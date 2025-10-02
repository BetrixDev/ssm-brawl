import type { PaginationOptions } from "@/schemas/pagination-options";
import { sql } from "drizzle-orm";
import { db, Table } from "..";

export function searchPlayerUsernames(query: string, paginationOptions: PaginationOptions) {
  const sanitized = query.replace(/\s+/g, ' ').trim();
  const matchQuery = sanitized.length ? sanitized + '*' : '';

  return db
    .select()
    .from(Table.players)
    .where(
      sql`${sql.raw('rowid')} IN (SELECT rowid FROM players_fts WHERE players_fts MATCH ${matchQuery})`,
    )
    .orderBy(
      sql`(SELECT bm25(players_fts)
           FROM players_fts
           WHERE players_fts.rowid = ${sql.raw('rowid')} AND players_fts MATCH ${matchQuery}
          ) ASC`,
    )
    .limit(paginationOptions.limit)
    .offset((paginationOptions.page - 1) * paginationOptions.limit);
}
