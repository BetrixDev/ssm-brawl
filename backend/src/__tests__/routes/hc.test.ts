import { describe, expect, it } from "bun:test";
import { app } from "../..";

describe("/hc", () => {
  it("returns Server is healthy", async () => {
    const reponse = await app.handle(new Request("http://localhost:1337/hc"));

    expect(reponse.status).toBe(200);

    const data = await reponse.json();
    expect(data).toEqual({
      message: "Server is healthy",
    });
  });
});
