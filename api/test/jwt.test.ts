import { expect, suite, test } from "vitest";
import { decodeTokenFromHeaders, generateBackendToken } from "../src/jwt.js";

suite("Jwt tests", () => {
  test("generateBackendToken should generate a token for use with the api", async () => {
    const token = await generateBackendToken("plugin");

    expect(token).toBeTypeOf("string");
  });

  test("decodeTokenFromHeaders should properly decode a signed token passed in with cookies", async () => {
    const token = await generateBackendToken("plugin");

    const header = new Headers();

    header.set("Cookie", `token=${token}`);

    const decoded = await decodeTokenFromHeaders(header);

    expect(decoded?.source).toBe("plugin");
  });

  test("decodeTokenFromHeaders should properly decode a signed token passed in with authorization header", async () => {
    const token = await generateBackendToken("plugin");

    const header = new Headers();

    header.set("Authorization", `Bearer ${token}`);

    const decoded = await decodeTokenFromHeaders(header);

    expect(decoded?.source).toBe("plugin");
  });

  test("decodeTokenFromHeaders should return undefined when no token is passed in either place", async () => {
    const decoded = await decodeTokenFromHeaders(new Headers());

    expect(decoded).toBeUndefined();
  });
});
