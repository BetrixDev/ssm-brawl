import {
  abilitiesTables,
  and,
  db,
  eq,
  gte,
  kitsTable,
  lte,
  mapsTable,
  minigamesTables,
  ongoingGamesTable,
  or,
  playersTables,
} from "@/db";
import { passivesTable } from "@/db/schemas/passives";
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
    .select({
      uuid: playersTables.uuid,
      kit: {
        id: kitsTable.id,
        displayName: kitsTable.displayName,
        inventoryIcon: kitsTable.inventoryIcon,
        visualArmor: kitsTable.visualArmor,
        passives: kitsTable.passives,
        abilities: kitsTable.abilities,
        damage: kitsTable.damage,
        armor: kitsTable.armor,
        knockback: kitsTable.knockback,
      },
    })
    .from(playersTables)
    .where(or(...body.playerUuids.map((u) => eq(playersTables.uuid, u))))
    .innerJoin(kitsTable, eq(kitsTable.id, playersTables.selectedKit))
    .execute();

  if (players.length !== body.playerUuids.length) {
    return new Response(undefined, {
      status: 400,
    });
  }

  const abilitiesToQuery = players.reduce<string[]>(
    (acc, curr) => [...acc, ...curr.kit.abilities],
    []
  );

  const passivesToQuery = players.reduce<string[]>(
    (acc, curr) => [...acc, ...curr.kit.passives],
    []
  );

  const abilitiesData = await db
    .select()
    .from(abilitiesTables)
    .where(or(...abilitiesToQuery.map((a) => eq(abilitiesTables.id, a))))
    .execute()
    .then((data) =>
      data.reduce<Record<string, typeof abilitiesTables.$inferSelect>>(
        (acc, curr) => ({ ...acc, [curr.id]: curr }),
        {}
      )
    );

  const passivesData = await db
    .select()
    .from(passivesTable)
    .where(or(...passivesToQuery.map((a) => eq(passivesTable.id, a))))
    .execute()
    .then((data) =>
      data.reduce<Record<string, typeof passivesTable.$inferSelect>>(
        (acc, curr) => ({ ...acc, [curr.id]: curr }),
        {}
      )
    );

  const injectedPlayerData = players.map((player) => {
    const kitAbilities = player.kit.abilities.map((id) => abilitiesData[id]!!);
    const kitPassives = player.kit.passives.map((id) => passivesData[id]!!);

    return {
      ...player,
      kit: { ...player.kit, abilities: kitAbilities, passives: kitPassives },
    };
  });

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

  const gameId = await db
    .insert(ongoingGamesTable)
    .values({
      playerUuids: body.playerUuids,
      modeId: body.modeId,
      isRanked: body.isRanked,
    })
    .execute();

  const ongoingGame = await db
    .select()
    .from(ongoingGamesTable)
    .where(eq(ongoingGamesTable.gameId, gameId.insertId))
    .execute();

  return new Response(
    JSON.stringify({
      ...ongoingGame,
      players: injectedPlayerData,
      map: selectedMap,
    })
  );
}
