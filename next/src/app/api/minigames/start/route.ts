import {
  and,
  db,
  eq,
  gte,
  lte,
  mapsTable,
  minigamesTables,
  ongoingGamesTable,
  or,
  playersTables,
  queueTable,
} from "@/db";
import { getRandomElement, isAuthedForRequest } from "@/utils";
import { z } from "zod";

const requestSchema = z.object({
  playerUuids: z.array(z.string()),
  modeId: z.string(),
  isRanked: z.boolean(),
});

export async function POST(request: Request) {
  if (!isAuthedForRequest(request)) {
    return new Response(undefined, {
      status: 403,
    });
  }

  const body = requestSchema.parse(await request.json());

  const [miniGame] = await db
    .select()
    .from(minigamesTables)
    .where(eq(minigamesTables.modeId, body.modeId))
    .execute();

  if (miniGame === undefined) {
    return new Response(undefined, {
      status: 400,
    });
  }

  const players = await db
    .select()
    .from(playersTables)
    .where(or(...body.playerUuids.map((u) => eq(playersTables.uuid, u))))
    .execute();

  if (players.length !== body.playerUuids.length) {
    return new Response(undefined, {
      status: 400,
    });
  }

  // await db
  //   .delete(queueTable)
  //   .where(or(...body.playerUuids.map((u) => eq(playersTables.uuid, u))))
  //   .execute();

  const validMaps = await db
    .select()
    .from(mapsTable)
    .where(
      and(
        eq(mapsTable.isHidden, false),
        lte(mapsTable.minPlayerCount, players.length),
        gte(mapsTable.maxPlayerCount, players.length),
        body.isRanked ? eq(mapsTable.canBeRanked, true) : undefined
      )
    )
    .execute();

  const selectedMap = getRandomElement(validMaps);

  const ongoingGame = await db
    .insert(ongoingGamesTable)
    .values({
      playerUuids: body.playerUuids,
      modeId: body.modeId,
      isRanked: body.isRanked,
    })
    .returning()
    .execute()
    .then((g) => g[0]!);

  return new Response(
    JSON.stringify({ ...ongoingGame, players, map: selectedMap })
  );
}
