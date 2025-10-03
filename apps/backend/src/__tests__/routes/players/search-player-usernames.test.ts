import { db, initDb, Table } from "@/db";
import { appRouter } from "@/routers";
import { call } from "@orpc/server";
import { beforeAll, describe, expect, it } from "bun:test";

const USERNAMES = [
  "OliviaSmith",
  "LiamJohnson",
  "EmmaWilliams",
  "NoahBrown",
  "AvaJones",
  "SophiaGarcia",
  "MasonMartinez",
  "IsabellaDavis",
  "LucasLopez",
  "MiaHernandez",
  "EthanGonzalez",
  "CharlotteWilson",
  "AmeliaMoore",
  "BenjaminTaylor",
  "HarperAnderson",
  "ElijahThomas",
  "EvelynJackson",
  "JamesWhite",
  "AbigailHarris",
  "HenryMartin",
];

describe("players/search-player-usernames", () => {
  beforeAll(async () => {
    await initDb();

    await db.insert(Table.players).values(
      USERNAMES.map((username, idx) => ({
        uuid: idx.toString(),
        username,
      })),
    );
  });

  it("returns players sorted by relevance to query", async () => {
    const response = await call(
      appRouter.players.searchPlayerUsernames,
      { query: "Ben" },
      { context: { headers: new Headers() } },
    );

    expect(response).toEqual([{ username: "BenjaminTaylor", uuid: "13" }]);
  });

  it("returns players sorted by relevance to query", async () => {
    const response = await call(
      appRouter.players.searchPlayerUsernames,
      { query: "li" },
      { context: { headers: new Headers() } },
    );

    expect(response).toEqual([
      { username: "LiamJohnson", uuid: "1" },
      { username: "OliviaSmith", uuid: "0" },
      { username: "EmmaWilliams", uuid: "2" },
      { username: "AmeliaMoore", uuid: "12" },
      { username: "ElijahThomas", uuid: "15" },
    ]);
  });
});
