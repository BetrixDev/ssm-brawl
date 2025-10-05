import type { PaginationOptions } from "@/schemas/pagination-options";
import { like, sql } from "drizzle-orm";
import { db, Table } from "../db";

export async function searchPlayerUsernames(query: string, paginationOptions?: PaginationOptions) {
  return await db
    .select({
      username: Table.players.username,
      uuid: Table.players.uuid,
    })
    .from(Table.players)
    .where(like(Table.players.username, `%${query}%`))
    .orderBy(
      sql`
        CASE
          WHEN username = ${query} THEN 0
          WHEN username LIKE ${query + "%"} THEN 1
          WHEN username LIKE ${"%" + query + "%"} THEN 2
          ELSE 3
        END
      `,
    )
    .limit(paginationOptions?.limit ?? 100)
    .offset(((paginationOptions?.page ?? 1) - 1) * (paginationOptions?.limit ?? 100));
}
