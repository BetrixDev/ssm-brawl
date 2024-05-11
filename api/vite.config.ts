import { defineConfig } from "vite";

export default defineConfig({
  test: {
    coverage: {
      extension: [".ts"],
    },
    globalSetup: ["./setup-tests.ts"],
    env: {
      NODE_ENV: "test",
      JWT_PRIVATE_KEY: "private_key",
      API_HOST: "localhost",
      API_PORT: "8080",
      API_PROTOCOL: "http",
      API_TOKEN_SECRET: "token_secret",
      TUSSLER_URL: "this_doesnt_matter",
      TUSSLER_TOKEN: "fake token",
      WRANGLER_HOST: "localhost",
      WRANGLER_PORT: "27017",
    },
  },
});
