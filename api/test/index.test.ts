import { env } from "env/api";
import { initTussler, runMirations, clearAllTables } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";

suite("Index tests", () => {
  const API_ENDPOINT = `${env.API_PROTOCOL}://${env.API_HOST}:${env.API_PORT}`;

  beforeEach(async (ctx) => {
    initTussler(ctx.task.name);
    await runMirations();
    await clearAllTables();
  });

  test("Importing index should not error", async () => {
    await import("../src/index.js");
  }, 30000);

  test("/health should return status code 200 when api is started", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/health`);

    expect(response.status).toEqual(200);
  }, 30000);

  test("/panel should return a static html page", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/panel`);

    expect(response.status).toEqual(200);
    expect(response.headers.get("Content-Type")?.includes("html")).toBeTruthy();
  }, 30000);

  test("/generateToken/:source should return a token in the cookies", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    expect(response.status).toEqual(200);
  }, 30000);

  test("/generateToken/:source should return a 403 status when the wrong secret is passed in", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: "bad token",
      },
    });

    expect(response.status).toEqual(403);
  }, 30000);

  test("/generateToken/:source should return a 400 status when the wrong source is passed in", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/generateToken/badSource`, {
      method: "post",
      headers: {
        Secret: "bad token",
      },
    });

    expect(response.status).toEqual(400);
  }, 30000);
});
