import { runMirations, clearAllTables, initTussler } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";
import { createInternalCaller } from "../test-utils.js";
import { kv } from "../../src/kv.js";
import { TRPCError } from "@trpc/server";

suite("Server router tests", () => {
  beforeEach(async (ctx) => {
    initTussler(ctx.task.name);
    await runMirations();
    await clearAllTables();
  });

  test("beginShutdown should perform shutdown procedure", async () => {
    const caller = createInternalCaller();

    await caller.server.beginShutdown();
  });

  test("beginShutdown should return 403 when already shutting down", async () => {
    const caller = createInternalCaller();

    await kv.setItem("isShuttingDown", true);

    await expect(async () => {
      await caller.server.beginShutdown();
    }).rejects.toThrow(TRPCError);
  });
});
