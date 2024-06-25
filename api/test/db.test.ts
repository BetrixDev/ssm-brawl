import {
  basicPlayerData,
  clearAllTables,
  db,
  initTussler,
  libsqlClient,
  loadTestTableData,
  runMigrations,
} from "tussler";
import { beforeEach, expect, suite, test } from "vitest";

suite("Tussler Test Enviroment Validation", () => {
  beforeEach(async () => {
    initTussler();
    await runMigrations();
    await clearAllTables();
  });

  test("Validate database is cleared", async () => {
    const initialDbValues = await db.query.basicPlayerData.findMany({});

    expect(initialDbValues.length).toEqual(0);
  });

  test("Validate inserting data into database works", async () => {
    const TEST_UUID = "testUuid";

    await db.insert(basicPlayerData).values({
      uuid: TEST_UUID,
    });

    const data = await db.query.basicPlayerData.findFirst({
      where: (table, { eq }) => eq(table.uuid, TEST_UUID),
    });

    expect(data?.uuid).toEqual(TEST_UUID);
  });

  test("Load data from json file", async () => {
    const loadedData = await loadTestTableData("basicPlayerData-1");

    const data = await db.query.basicPlayerData.findFirst({
      where: (table, { eq }) => eq(table.uuid, "this_is_a_test"),
    });

    expect(data?.uuid).toEqual("this_is_a_test");
    expect(data?.rankElo).toEqual(loadedData[0].rankElo);
  });
});
