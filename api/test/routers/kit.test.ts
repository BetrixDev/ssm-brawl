import { expect, suite, test, vi } from "vitest";
import { createInternalCaller } from "../test-utils.js";

suite("Kit router tests", () => {
  test("getKitPlaytimeMillis should return kit playtime", async () => {
    vi.mock("wrangler", () => {
      return {
        wranglerClient: {
          aggregate: () => ({ tryNext: vi.fn(() => ({ kitPlaytime: 1000 })) }),
        },
      };
    });

    const caller = createInternalCaller();

    const result = await caller.kits.getKitPlaytimeMillis({
      kitId: "test_kit",
    });

    expect(result).toEqual(1000);
  });
});
