import { beforeEach, expect, suite, test } from "vitest";
import { clearAllTables, db, initTussler, loadTestTableData, runMigrations } from "tussler";
import { createInternalCaller } from "../test-utils.js";
import { TRPCError } from "@trpc/server";

suite("Player router tests", () => {
  beforeEach(async () => {
    await initTussler();
    await runMigrations();
    await clearAllTables();
  });

  test("getBasicPlayerData with new player", async () => {
    const caller = createInternalCaller();
    const playerUuid = "new_uuid";

    const result = await caller.player.getBasicPlayerData({ playerUuid: playerUuid });

    expect(result.firstTime).toBeTruthy();
    expect(result.uuid).toEqual(playerUuid);
  });

  test("getBasicPlayerData with recurring player", async () => {
    const caller = createInternalCaller();
    const testData = await loadTestTableData("basicPlayerData-1");

    const result = await caller.player.getBasicPlayerData({ playerUuid: testData[0].uuid });

    expect(result.firstTime).toBeFalsy();
    expect(result.uuid).toEqual(testData[0].uuid);
  });

  test("updatePlayerName with new player", async () => {
    const caller = createInternalCaller();
    const testUsername = "Test Username";
    const testUuid = "test_uuid";

    const result = await caller.player.updatePlayerName({
      playerUuid: testUuid,
      username: testUsername,
    });

    const dbResult = await db.query.usercache.findFirst({
      where: (table, { eq }) => eq(table.uuid, testUuid),
    });

    expect(result.username).toEqual(testUsername);
    expect(dbResult?.username).toEqual(testUsername);
  });

  test("updatePlayerName with existing player", async () => {
    await loadTestTableData("basicPlayerData-1");
    const testData = await loadTestTableData("usercache-1");
    const caller = createInternalCaller();
    const updatedUsername = "New username";

    await caller.player.updatePlayerName({
      playerUuid: testData[0].uuid,
      username: updatedUsername,
    });

    const dbResult = await db.query.usercache.findFirst({
      where: (table, { eq }) => eq(table.uuid, testData[0].uuid),
    });

    expect(dbResult?.username).toEqual(updatedUsername);
  });

  test("getDetailedPlayerData should return player data", async () => {
    const playerDataTestData = await loadTestTableData("basicPlayerData-1");
    await loadTestTableData("usercache-1");
    const caller = createInternalCaller();

    const result = await caller.player.getDetailedPlayerData({
      playerUuid: playerDataTestData[0].uuid,
    });

    expect(result.uuid).toEqual(playerDataTestData[0].uuid);
  });

  test("getDetailedPlayerData should throw error when player doesn't exist", async () => {
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.player.getDetailedPlayerData({
        playerUuid: "unknown_uuid",
      });
    }).rejects.toThrow(TRPCError);
  });

  test("isIpBanned should return false for an ip that isn't banned", async () => {
    const caller = createInternalCaller();

    const result = await caller.player.isIpBanned({
      ip: "127.0.0.1",
    });

    expect(result.isBanned).toBeFalsy();
  });

  test("isIpBanned should return true for an ip that is banned", async () => {
    const testData = await loadTestTableData("ipBans-1");
    const caller = createInternalCaller();

    const result = await caller.player.isIpBanned({
      ip: testData[0].ip,
    });

    expect(result.isBanned).toBeTruthy();
  });

  test("isIpBanned should ban the passed in uuid for an ip that is banned", async () => {
    const playerTestData = await loadTestTableData("basicPlayerData-1");
    const testData = await loadTestTableData("ipBans-1");
    const caller = createInternalCaller();

    const initialPlayerResult = await caller.player.getBasicPlayerData({
      playerUuid: playerTestData[0].uuid,
    });

    expect(initialPlayerResult.isBanned).toBeFalsy();

    await caller.player.isIpBanned({
      ip: testData[0].ip,
      playerUuid: playerTestData[0].uuid,
    });

    const playerResult = await caller.player.getBasicPlayerData({
      playerUuid: playerTestData[0].uuid,
    });

    expect(playerResult.isBanned).toBeTruthy();
  });

  test("isIpBanned shouldn't error when invalid uuid is passed in with banned ip", async () => {
    const testData = await loadTestTableData("ipBans-1");
    const caller = createInternalCaller();

    const result = await caller.player.isIpBanned({
      ip: testData[0].ip,
      playerUuid: "random uuid",
    });

    expect(result.isBanned).toBeTruthy();
  });
});
