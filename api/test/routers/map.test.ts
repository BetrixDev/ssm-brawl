import { runMirations, clearAllTables, loadTestTableData, initTussler } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";
import { createInternalCaller } from "../test-utils.js";
import { TRPCError } from "@trpc/server";

suite("Map router testing", () => {
  beforeEach(async (ctx) => {
    initTussler(ctx.task.name);
    await runMirations();
    await clearAllTables();
  });

  test("getMapDetails should return map details", async () => {
    const testData = await loadTestTableData("maps-1");
    const caller = createInternalCaller();

    const result = await caller.maps.getMapDetails({
      mapId: testData[0].id,
    });

    expect(result.id).toEqual(testData[0].id);
  });

  test("getMapDetails should error on invalid map id", async () => {
    const caller = createInternalCaller();

    await expect(async () => {
      await caller.maps.getMapDetails({
        mapId: "bad id",
      });
    }).rejects.toThrow(TRPCError);
  });
});
