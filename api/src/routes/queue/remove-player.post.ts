import { db, eq, queueTable } from "db";
import * as v from "valibot";

const bodySchema = v.object({
  playerUuid: v.string(),
});

export default defineEventHandler(async (event) => {
  const body = v.parse(bodySchema, await readBody(event));

  await db.delete(queueTable).where(eq(queueTable.playerId, body.playerUuid));
});
