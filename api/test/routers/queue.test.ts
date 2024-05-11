import { initTussler, runMirations, clearAllTables, loadTestTableData } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";
import { createInternalCaller } from "../test-utils.js";
import { kv, loadDefaultKvValues } from "../../src/kv.js";
import { TRPCError } from "@trpc/server";

suite("Queue router tests", () => {
  beforeEach(async (ctx) => {
    initTussler(ctx.task.name);
    await runMirations();
    await clearAllTables();
    await loadDefaultKvValues();
  });

  test("addPlayer should add a player to the queue", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const minigameTestData = await loadTestTableData("minigames-1");
    const caller = createInternalCaller();

    const result = await caller.queue.addPlayer({
      minigameId: minigameTestData[0].id,
      playerUuid: playerTestData[0].uuid,
    });

    expect(result.type).toEqual("added");
  });

  test("addPlayer should error when server is shutting down", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const minigameTestData = await loadTestTableData("minigames-1");
    const caller = createInternalCaller();

    await kv.setItem("isShuttingDown", true);

    await expect(async () => {
      await caller.queue.addPlayer({
        minigameId: minigameTestData[0].id,
        playerUuid: playerTestData[0].uuid,
      });
    }).rejects.toThrow(TRPCError);
  });

  test("addPlayer should error when player is already in queue", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const minigameTestData = await loadTestTableData("minigames-1");
    const caller = createInternalCaller();

    await caller.queue.addPlayer({
      minigameId: minigameTestData[0].id,
      playerUuid: playerTestData[0].uuid,
    });

    await expect(async () => {
      await caller.queue.addPlayer({
        minigameId: minigameTestData[0].id,
        playerUuid: playerTestData[0].uuid,
      });
    }).rejects.toThrow(TRPCError);
  });

  test("addPlayer should add the player to the queue when force is true", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const minigameTestData = await loadTestTableData("minigames-1");
    const caller = createInternalCaller();

    await caller.queue.addPlayer({
      minigameId: minigameTestData[0].id,
      playerUuid: playerTestData[0].uuid,
    });

    const result = await caller.queue.addPlayer({
      minigameId: minigameTestData[0].id,
      playerUuid: playerTestData[0].uuid,
      force: true,
    });

    expect(result).toBeTruthy();
  });

  test("addPlayer should error when minigame id is bad value", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.queue.addPlayer({
        minigameId: "bad id",
        playerUuid: playerTestData[0].uuid,
      });
    }).rejects.toThrow(TRPCError);
  });
});
