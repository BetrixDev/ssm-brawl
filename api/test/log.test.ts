import { suite, test } from "vitest";
import { log } from "../src/log.js";

suite("Log tests", () => {
  test("General logger test", () => {
    log.info("hello");
  });
});
