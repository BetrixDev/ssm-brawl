import { env } from "env/api";
import { initTussler, runMigrations, clearAllTables } from "tussler";
import { beforeEach, expect, suite, test } from "vitest";

suite("Index tests", () => {
  const API_ENDPOINT = `${env.API_PROTOCOL}://${env.API_HOST}:${env.API_PORT}`;

  beforeEach(async () => {
    initTussler();
    await runMigrations();
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

  test("/trpc/ should reject unauthorized requests", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/trpc/health`);

    expect(response.status).toEqual(401);
  });

  test("/trpc/ should map function when recieving authorized request", async () => {
    await import("../src/index.js");

    const tokenResponse = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    const tokenText = await tokenResponse.text();

    const response = await fetch(`${API_ENDPOINT}/trpc/health`, {
      headers: {
        Cookie: `token=${tokenText}`,
      },
    });

    expect(response.status).toEqual(200);
  });

  test("/api/ should reject unauthorized requests", async () => {
    await import("../src/index.js");

    const response = await fetch(`${API_ENDPOINT}/api/player.getBasicPlayerData`);

    expect(response.status).toEqual(401);
  });

  test("/api/ should reject requests not from plugin as a source", async () => {
    await import("../src/index.js");

    const tokenResponse = await fetch(`${API_ENDPOINT}/generateToken/brawlie`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    const tokenText = await tokenResponse.text();

    const response = await fetch(`${API_ENDPOINT}/api/health`, {
      headers: {
        Cookie: `token=${tokenText}`,
      },
    });

    expect(response.status).toEqual(401);
  });

  test("/api/ should return 404 when bad procedure name is passed in", async () => {
    await import("../src/index.js");

    const tokenResponse = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    const tokenText = await tokenResponse.text();

    const response = await fetch(`${API_ENDPOINT}/api/bad.procedure`, {
      headers: {
        Cookie: `token=${tokenText}`,
      },
    });

    expect(response.status).toEqual(404);
  });

  test("/api/ should return 200 when happy", async () => {
    await import("../src/index.js");

    const tokenResponse = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    const tokenText = await tokenResponse.text();

    const response = await fetch(`${API_ENDPOINT}/api/health`, {
      headers: {
        Cookie: `token=${tokenText}`,
      },
    });

    expect(response.status).toEqual(200);
  });

  test("/api/ should propagate errors from routers to client", async () => {
    await import("../src/index.js");

    const tokenResponse = await fetch(`${API_ENDPOINT}/generateToken/plugin`, {
      method: "post",
      headers: {
        Secret: env.API_TOKEN_SECRET,
      },
    });

    const tokenText = await tokenResponse.text();

    const response = await fetch(`${API_ENDPOINT}/api/player.getBasicPlayerData`, {
      headers: {
        Cookie: `token=${tokenText}`,
      },
    });

    expect(response.status).toEqual(400);
  });
});
