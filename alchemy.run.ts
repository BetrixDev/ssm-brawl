import alchemy from "alchemy";
import { Astro, KVNamespace, Vite, Worker } from "alchemy/cloudflare";
import { GitHubComment } from "alchemy/github";
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

const neonDb = await NeonProject("db", {
  name: `${app.name}-${app.stage}-db`,
  apiKey: alchemy.secret(process.env.NEON_API_KEY),
  region_id: "aws-us-east-1",
  pg_version: 18 as any,
});

await Exec("db-generate", {
  cwd: "apps/backend",
  command: "pnpm run db:generate",
  env: {
    DATABASE_URL: neonDb.connection_uris[0].connection_uri,
  },
});

const kv = await KVNamespace("kv", {
  title: `${app.name}-${app.stage}-kv`,
});

await Exec("db-generate", {
  cwd: "apps/backend",
  command: `pnpm run db:migrate`,
  env: {
    DATABASE_URL: neonDb.connection_uris[0].connection_uri,
  },
});

let apiDomainName = "api.ssmbrawl.com";

if (app.stage !== "prod") {
  apiDomainName = `${app.stage}-api.ssmbrawl.com`;
}

export const backend = await Worker("backend", {
  cwd: "apps/backend",
  entrypoint: "src/index.ts",
  compatibility: "node",
  bindings: {
    NODE_ENV: app.stage === "prod" ? "production" : "development",
    DATABASE_URL: neonDb.connection_uris[0].connection_uri,
    KV: kv,
    CORS_ORIGIN: process.env.CORS_ORIGIN || "http://localhost:3001",
    BETTER_AUTH_SECRET: alchemy.secret(process.env.BETTER_AUTH_SECRET),
    BETTER_AUTH_URL: process.env.BETTER_AUTH_URL || "",
    POLAR_ACCESS_TOKEN: alchemy.secret(process.env.POLAR_ACCESS_TOKEN),
    POLAR_SUCCESS_URL: process.env.POLAR_SUCCESS_URL || "",
    POLAR_SERVER: process.env.POLAR_SERVER || "sandbox",
    PLUGIN_SECRET_KEY: alchemy.secret(process.env.PLUGIN_SECRET_KEY),
  },
  domains:
    app.stage !== "dev"
      ? [
          {
            domainName: apiDomainName,
            zoneId: process.env.CLOUDFLARE_ZONE_ID,
          },
        ]
      : undefined,
  dev: {
    port: 3000,
  },
  name: `${app.name}-${app.stage}-backend`,
});

let webDomainName = "ssmbrawl.com";

if (app.stage !== "prod") {
  webDomainName = `${app.stage}-ssmbrawl.com`;
}

export const web = await Vite("web", {
  name: `${app.name}-${app.stage}-web`,
  cwd: "apps/web",
  bindings: {
    VITE_SERVER_URL: removeTrailingSlash(process.env.VITE_SERVER_URL || backend.url) || "",
  },
  dev: {
    command: "pnpm run dev",
    env: {
      VITE_SERVER_URL: removeTrailingSlash(backend.url) || "http://localhost:3000",
    },
  },
  compatibilityFlags: ["nodejs_compat"],
  compatibilityDate: "2025-09-02",
  entrypoint: "@tanstack/react-start/server-entry",
  domains:
    app.stage !== "dev"
      ? [
          {
            domainName: webDomainName,
            zoneId: process.env.CLOUDFLARE_ZONE_ID,
          },
        ]
      : undefined,
});

const wikiSessionKv = await KVNamespace("wiki-session", {
  title: `${app.name}-${app.stage}-wiki-session`,
});

let wikiDomainName = "wiki.ssmbrawl.com";

if (app.stage !== "prod") {
  wikiDomainName = `${app.stage}-wiki.ssmbrawl.com`;
}

export const wiki = await Astro("wiki", {
  name: `${app.name}-${app.stage}-wiki`,
  cwd: "apps/wiki",
  dev: {
    command: "pnpm run dev",
  },
  bindings: {
    SESSION: wikiSessionKv,
  },
  domains:
    app.stage !== "dev"
      ? [
          {
            domainName: wikiDomainName,
            zoneId: process.env.CLOUDFLARE_ZONE_ID,
          },
        ]
      : undefined,
});

console.log(`Backend -> ${backend.url}`);
console.log(`Web -> ${web.url}`);
console.log(`Wiki -> ${wiki.url}`);

if (process.env.PULL_REQUEST) {
  await GitHubComment("preview-comment", {
    owner: "BetrixDev",
    repository: "ssm-brawl",
    issueNumber: Number(process.env.PULL_REQUEST),
    body: `
     ## 🚀 Preview Deployed

     Your changes have been deployed to a preview environment:

     **🌐 Website:** ${web.url}
     **🌐 Backend:** ${backend.url}
     **🌐 Wiki:** ${wiki.url}

     Built from commit ${process.env.GITHUB_SHA?.slice(0, 7)}

     ---
     <sub>🤖 This comment updates automatically with each push.</sub>`,
  });
}

await app.finalize();

function removeTrailingSlash(url?: string) {
  if (!url) {
    return undefined;
  }

  return url.endsWith("/") ? url.slice(0, -1) : url;
}
