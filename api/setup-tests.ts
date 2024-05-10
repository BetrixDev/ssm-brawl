/* c8 ignore start */

import path from "path";
import { rmSync, mkdirSync } from "fs";

const TEST_DB_PATH = path.join(process.cwd(), "test_dbs");

export function setup() {
  try {
    rmSync(TEST_DB_PATH, { recursive: true, force: true });
  } catch {}

  try {
    mkdirSync(TEST_DB_PATH);
  } catch {}
}

/* c8 ignore end */
