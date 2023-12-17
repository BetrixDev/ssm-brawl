import { db, eq, playersTables } from "@/db";
import { isAuthedForRequest } from "@/utils";
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

  let playerData: typeof playersTables.$inferSelect;

  const [dbSelect] = await db
    .select()
    .from(playersTables)
    .where(eq(playersTables.uuid, body.uuid))
    .execute();

  if (dbSelect !== undefined) {
    playerData = dbSelect;
  } else {
    const playerId = await db
      .insert(playersTables)
      .values({ uuid: body.uuid })
      .execute();

    playerData = await db
      .select()
      .from(playersTables)
      .where(eq(playersTables.uuid, playerId.insertId))
      .limit(1)
      .execute()
      .then((p) => p[0]!);
  }

  return new Response(JSON.stringify(playerData));
}
