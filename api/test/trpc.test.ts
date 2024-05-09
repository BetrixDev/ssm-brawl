import { expect, suite, test } from "vitest";
import { t } from "../src/trpc.js";
import { appRouter } from "../src/routers/router.js";
import { TRPCError } from "@trpc/server";

suite("TRPC testing", () => {
  test("Throw error when source is user on internal procedure", async () => {
    await expect(async () => {
      const caller = t.createCallerFactory(appRouter)({
        claims: { iat: Date.now(), source: "user", uuid: "random_uuid" },
        resHeaders: new Headers(),
      });

      await caller.player.getBasicPlayerData({ playerUuid: "random_uuid" });
    }).rejects.toThrow(TRPCError);
  });
});
