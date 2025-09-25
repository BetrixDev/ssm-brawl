import alchemy from "alchemy";
import { D1Database, KVNamespace, Nextjs, Vite, Worker } from "alchemy/cloudflare";
import { Exec } from "alchemy/os";
import { config } from "dotenv";

config({ path: "./.env" });
config({ path: "./apps/backend/.env" });
config({ path: "./apps/web/.env" });

const app = await alchemy("super-smash-mobs-brawl");

await Exec("db-generate", {
  cwd: "apps/backend",
  command: "pnpm run db:generate",
});

const kv = await KVNamespace("kv", {
  adopt: true,
});

const db = await D1Database("database", {
  adopt: true,
  migrationsDir: "apps/backend/src/db/migrations",
});

export const backend = await Worker("backend", {
  adopt: true,
  cwd: "apps/backend",
  entrypoint: "src/index.ts",
  compatibility: "node",
  bindings: {
    DB: db,
    KV: kv,
    CORS_ORIGIN: process.env.CORS_ORIGIN || "http://localhost:3001",
    BETTER_AUTH_SECRET: alchemy.secret(process.env.BETTER_AUTH_SECRET),
    BETTER_AUTH_URL: process.env.BETTER_AUTH_URL || "",
    POLAR_ACCESS_TOKEN: alchemy.secret(process.env.POLAR_ACCESS_TOKEN),
    POLAR_SUCCESS_URL: process.env.POLAR_SUCCESS_URL || "",
    POLAR_SERVER: process.env.POLAR_SERVER || "sandbox",
  },
  domains: [
    {
      domainName: "api.ssmbrawl.com",
      adopt: true,
    },
  ],
  dev: {
    port: 3000,
  },
  name: "ssmbrawl-backend",
});

export const web = await Vite("web", {
  adopt: true,
  cwd: "apps/web",
  assets: "dist",
  bindings: {
    VITE_SERVER_URL: process.env.VITE_SERVER_URL || backend.url || "",
  },
  dev: {
    command: "pnpm run dev",
    env: {
      VITE_SERVER_URL: removeTrailingSlash(backend.url) || "http://localhost:3000",
    },
  },
  domains: [
    {
      domainName: "ssmbrawl.com",
      adopt: true,
    },
  ],
  compatibilityDate: "2025-09-02",
  compatibilityFlags: ["nodejs_compat"],
  name: "ssmbrawl-web",
});

export const wiki = await Nextjs("wiki", {
  adopt: true,
  cwd: "apps/wiki",
  dev: {
    command: "pnpm run dev",
  },
});

console.log(`Backend -> ${backend.url}`);
console.log(`Web -> ${web.url}`);
console.log(`Wiki -> ${wiki.url}`);

await app.finalize();

function removeTrailingSlash(url?: string) {
  if (!url) {
    return undefined;
  }

  return url.endsWith("/") ? url.slice(0, -1) : url;
}
