import { runMigrations, clearAllTables, loadTestTableData, initTussler, db } from "tussler";
import { beforeEach, expect, suite, test, vi } from "vitest";
import { createInternalCaller } from "../test-utils.js";
import { TRPCError } from "@trpc/server";

suite("Minigame router tests", async () => {
  beforeEach(async () => {
    await initTussler();
    await runMigrations();
    await clearAllTables();
  });

  test("start should start a minigame", async () => {
    const minigameTestData = await loadTestTableData("minigames-1");
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    await loadTestTableData("passives-1");
    await loadTestTableData("passivesToKits-1");
    await loadTestTableData("kits-1");
    const caller = createInternalCaller();

    const result = await caller.minigame.start({
      minigameId: minigameTestData[0].id,
      teams: [{ id: "test_team_id", players: [playerTestData[0].uuid] }],
    });

    expect(result.minigame.id).toEqual(minigameTestData[0].id);
  });

  test("start should error on invalid minigame id", async () => {
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.minigame.start({
        minigameId: "bad id",
        teams: [{ id: "test_team_id", players: ["test_uuid"] }],
      });
    }).rejects.toThrow(TRPCError);
  });

  test("start should error when a player doesn't exist", async () => {
    const minigameTestData = await loadTestTableData("minigames-1");
    await loadTestTableData("basicPlayerData-1");
    await loadTestTableData("passives-1");
    await loadTestTableData("passivesToKits-1");
    await loadTestTableData("kits-1");
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.minigame.start({
        minigameId: minigameTestData[0].id,
        teams: [{ id: "test_team_id", players: ["bad_uuid"] }],
      });
    }).rejects.toThrow(TRPCError);
  });

  test("getPlayableGames should not error", async () => {
    await loadTestTableData("minigames-1");
    const caller = createInternalCaller();

    await caller.minigame.getPlayableGames();
  });

  test("end should not error when called", async () => {
    vi.mock("wrangler", () => {
      return {
        wranglerClient: {
          getRepository: () => ({ save: async () => ({}) }),
        },
      };
    });

    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const caller = createInternalCaller();

    await caller.minigame.end({
      gameId: "test_game_id",
      mapId: "test_map",
      minigameId: "test_minigame",
      players: [
        {
          kits: [
            {
              id: "test_kit",
              abilityUsage: [{ abilityId: "test_ability", usedAt: Date.now(), damageDealt: 10 }],
              endTime: Date.now(),
              startTime: Date.now(),
            },
          ],
          teamId: "test_team",
          uuid: playerTestData[0].uuid,
          stocksLeft: 4,
          leftInProgress: false,
        },
      ],
      winningUuids: [playerTestData[0].uuid],
    });

    const playerDbResult = await db.query.basicPlayerData.findFirst({
      where: (table, { eq }) => eq(table.uuid, playerTestData[0].uuid),
    });

    expect(playerDbResult?.totalGamesPlayed).toEqual(1);
    expect(playerDbResult?.totalGamesWon).toEqual(1);
  });
});
