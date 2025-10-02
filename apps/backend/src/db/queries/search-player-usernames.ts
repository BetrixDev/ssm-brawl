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
    .limit(paginationOptions.limit)
    .offset((paginationOptions.page - 1) * paginationOptions.limit);
}
