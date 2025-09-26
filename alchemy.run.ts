import alchemy from "alchemy";
import { KVNamespace, Vite, Worker } from "alchemy/cloudflare";
import { NeonProject } from "alchemy/neon";
import { Exec } from "alchemy/os";
import { CloudflareStateStore } from "alchemy/state";

import { config } from "dotenv";

config({ path: "./.env" });
config({ path: "./apps/backend/.env" });
config({ path: "./apps/web/.env" });

const stage = process.env.STAGE ?? "dev";

const app = await alchemy("super-smash-mobs-brawl", {
  stateStore: (scope) =>
    new CloudflareStateStore(scope, {
      scriptName: `ssmbrawl-state-${stage}`,
    }),
});

// const serverImage = await docker.Image("minecraft", {
//   name: "ssmbrawl-minecraft",
//   tag: "latest",
//   build: {
//     context: "./plugin",
//   },
// });

const server = await Exec("plugin-dev", {
  cwd: "plugin",
  command: "pnpm dev",
});

await Exec("db-generate", {
  cwd: "apps/backend",
  command: "pnpm run db:generate",
});

const kv = await KVNamespace("kv", {
  adopt: true,
});

const neonDb = await NeonProject("db", {
  name: `Super Smash Mobs Brawl ${stage}`,
  apiKey: alchemy.secret(process.env.NEON_API_KEY),
  region_id: "aws-us-east-1",
  pg_version: 18 as any,
});

await Exec("db-generate", {
  cwd: "apps/backend",
  command: `pnpm run db:migrate`,
  env: {
    DATABASE_URL: neonDb.connection_uris[0].connection_uri,
  },
});

export const backend = await Worker("backend", {
  adopt: true,
  cwd: "apps/backend",
  entrypoint: "src/index.ts",
  compatibility: "node",
  bindings: {
    DATABASE_URL: neonDb.connection_uris[0].connection_uri,
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

console.log(`Backend -> ${backend.url}`);
console.log(`Web -> ${web.url}`);

await app.finalize();

function removeTrailingSlash(url?: string) {
  if (!url) {
    return undefined;
  }

  return url.endsWith("/") ? url.slice(0, -1) : url;
}
