import { db, queueTable } from "@/db";
import { isAuthedForRequest } from "@/utils";
import { eq } from "drizzle-orm";
import { z } from "zod";

const requestSchema = z.object({
  uuid: z.string(),
});

export async function POST(request: Request) {
  if (!isAuthedForRequest(request)) {
    return new Response(undefined, {
      status: 403,
    });
  }

  const body = requestSchema.parse(await request.json());

  const [existingPlayerInQueue] = await db
    .select()
    .from(queueTable)
    .where(eq(queueTable.playerUuid, body.uuid))
    .execute();

  if (existingPlayerInQueue === undefined) {
    return new Response(undefined, {
      status: 409,
    });
  }

  await db
    .delete(queueTable)
    .where(eq(queueTable.playerUuid, body.uuid))
    .execute();

  return new Response(
    JSON.stringify({
      modeId: existingPlayerInQueue.modeId,
      isRanked: existingPlayerInQueue.isRanked,
    })
  );
}
