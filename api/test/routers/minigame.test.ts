import { runMirations, clearAllTables, loadTestTableData, initTussler } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";
import { createInternalCaller } from "../test-utils.js";
import { TRPCError } from "@trpc/server";

suite("Minigame router tests", async () => {
  beforeEach(async (ctx) => {
    initTussler(ctx.task.name);
    await runMirations();
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
      teams: [[playerTestData[0].uuid]],
    });

    expect(result.minigame.id).toEqual(minigameTestData[0].id);
  });

  test("start should error on invalid minigame id", async () => {
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.minigame.start({
        minigameId: "bad id",
        teams: [["test_uuid"]],
      });
    }).rejects.toThrow(TRPCError);
  });

  test("start should error when a player doesn't exist", async () => {
    const minigameTestData = await loadTestTableData("minigames-1");
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    await loadTestTableData("passives-1");
    await loadTestTableData("passivesToKits-1");
    await loadTestTableData("kits-1");
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.minigame.start({
        minigameId: minigameTestData[0].id,
        teams: [["test_uuid"]],
      });
    }).rejects.toThrow(TRPCError);
  });
});
