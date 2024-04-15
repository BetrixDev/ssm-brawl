import { Hono } from "hono";
import { serve } from "@hono/node-server";
import { Octokit } from "octokit";
import { env } from "env/orchestrator";
import path from "path";
import { existsSync, mkdirSync, readdirSync, rmSync, writeFileSync } from "fs";
import AdmZip from "adm-zip";
import pm from "picomatch";
import { execa } from "execa";
import axios from "axios";
import crypto from "crypto";
import { Logger, middlewareLogger } from "logger";
import { deleteProcessesByName, spawnProcess } from "proc";

const API_START_TIMEOUT = 15000;
const DEPLOYMENTS_DIR = path.join(process.cwd(), "..", "deployments");

const app = new Hono();
const log = new Logger("orchestrator");

app.use(middlewareLogger(log));

if (!existsSync(DEPLOYMENTS_DIR)) {
  mkdirSync(DEPLOYMENTS_DIR);
}

const github = new Octokit({
  auth: env.GITHUB_TOKEN,
});

async function getWorkflowIds() {
  const response = await github.rest.actions.listRepoWorkflows({
    owner: "BetrixDev",
    repo: "ssm-brawl",
  });

  return response.data.workflows.map((workflow) => workflow.id);
}

async function checkForFailedOrRunningActions() {
  const workflowsToChecks = await getWorkflowIds();

  for (const workflowId of workflowsToChecks) {
    const runs = await github.rest.actions.listWorkflowRuns({
      owner: "BetrixDev",
      repo: "ssm-brawl",
      workflow_id: workflowId,
    });

    const isLastRunSuccess =
      runs.data.workflow_runs[0].conclusion === "success";

    if (!isLastRunSuccess) {
      return true;
    }
  }

  return false;
}

async function getLastCommitSha() {
  const response = await github.rest.repos.getCommit({
    owner: "BetrixDev",
    repo: "ssm-brawl",
    ref: "main",
    per_page: 1,
  });

  return response.data.sha;
}

async function getLatestPluginAssetId() {
  try {
    const response = await github.rest.repos.getLatestRelease({
      owner: "BetrixDev",
      repo: "ssm-brawl",
    });

    return response.data.assets.find(({ name }) =>
      pm.isMatch(name, "ssmb*.jar"),
    )?.id;
  } catch {
    return;
  }
}

async function deployServices(deploymentPath: string) {
  const pluginAssetId = await getLatestPluginAssetId();

  if (!pluginAssetId) {
    log.error(
      "Failed to download get plugin asset id from release, aborting deployment",
    );
    return;
  }

  try {
    const pluginBinary = await github.rest.repos.getReleaseAsset({
      owner: "BetrixDev",
      repo: "ssm-brawl",
      asset_id: pluginAssetId!,
      headers: {
        Accept: "application/octet-stream",
      },
    });

    if (!pluginBinary.data) {
      log.error("Failed to download plugin jar, aborting deployment");
      return;
    }

    const pluginsDir = path.join(deploymentPath, "server", "plugins");

    if (!existsSync(pluginsDir)) {
      mkdirSync(pluginsDir, { recursive: true });
    }

    const pluginPath = path.join(pluginsDir, "ssmb.jar");

    writeFileSync(
      pluginPath,
      Buffer.from(pluginBinary.data as any as ArrayBuffer),
    );
  } catch (e) {
    writeFileSync("error.json", JSON.stringify(e));
    log.error("Failed to download plugin jar, aborting deployment");
    return;
  }

  log.info("Installing dependencies...");

  await execa("pnpm install", undefined, {
    cwd: deploymentPath,
    stdout: "inherit",
  });

  log.info("Building services...");

  await execa("pnpm build", { cwd: deploymentPath, stdout: "inherit" });

  log.info("Killing old processes...");

  try {
    await deleteProcessesByName("api");
    await deleteProcessesByName("server");
  } catch (e: any) {
    log.error({
      message: "Failed to kill old processes, aborting deployment",
      ...e,
    });
  }

  log.info("Starting api...");

  try {
    await spawnProcess({
      name: "api",
      env: process.env as any,
      cwd: path.join(deploymentPath, "api"),
      // script: "pnpm start --filter=api --log-prefix=none --log-order=stream",
      script: "node ./build/index.js",
      max_memory_restart: "4G",
    });
  } catch (e: any) {
    console.log(e);
    log.error({
      message: "Failed to spawn api proccess, aborting deployment",
      ...e,
    });
    return;
  }

  const attemptStart = Date.now();
  let apiStarted = false;

  while (!apiStarted) {
    if (Date.now() - attemptStart > API_START_TIMEOUT) {
      log.error("API failed to start within the timeout, aborting deployment");
      return;
    }

    try {
      const response = await axios("http://localhost:${env.API_PORT}/health");

      if (response.status === 200) {
        apiStarted = true;
      }
    } catch {}

    await new Promise((res) => setTimeout(res, 500));
  }

  log.info("Starting the rest of the services...");

  try {
    await spawnProcess({
      name: "server",
      env: process.env as any,
      cwd: process.cwd(),
      script: "pnpm start --filter=server --log-prefix=none --log-order=stream",
      max_memory_restart: "4G",
    });
  } catch (e: any) {
    log.error({
      message: "Failed to spawn server proccess, aborting deployment",
      ...e,
    });
    return;
  }

  log.info("Deleting previous deployments...");

  readdirSync(path.join(deploymentPath, ".."), {
    withFileTypes: true,
  })
    .filter((dirent) => dirent.isDirectory() && deploymentPath !== dirent.path)
    .forEach((dirent) => {
      rmSync(dirent.path, { recursive: true, force: true });
    });
}

async function onActionFinished() {
  log.info("Checking for failed or running actions...");

  const isFailedOrRunningActions = await checkForFailedOrRunningActions();

  if (isFailedOrRunningActions) {
    log.error("Failed or running actions found, aborting deployment");
    return;
  }

  log.info("No failed or running actions found, proceeding with deployment");
  log.info("Downloading deployment contents...");

  const deploymentId = await getLastCommitSha();
  const deloymentContentsResponse =
    await github.rest.repos.downloadZipballArchive({
      owner: "BetrixDev",
      repo: "ssm-brawl",
      ref: "main",
    });

  log.info("Unzipping deployment contents...");

  const archive = new AdmZip(
    Buffer.from(deloymentContentsResponse.data as ArrayBuffer),
  );

  const entries = archive.getEntries();

  const ignoredGlobs =
    entries
      .find((e) => e.entryName.includes(".deployignore"))
      ?.getData()
      .toString("utf-8")
      .split("\n")
      .filter((str) => str.length > 0) ?? [];

  function shouldIgnoreFile(path: string) {
    return ignoredGlobs.some((glob) =>
      // Replace periods with a random character when glob is within
      //  a directory since otherwise matching is not consistent
      pm.isMatch(glob.includes("/") ? path.replace(".", "a") : path, glob),
    );
  }

  const deploymentDir = path.join(
    DEPLOYMENTS_DIR,
    `${Date.now()}-${deploymentId}`,
  );

  mkdirSync(deploymentDir);

  for (const entry of entries) {
    const entryNamePath = entry.entryName.split("/").slice(1).join("/");

    if (shouldIgnoreFile(entryNamePath)) continue;

    if (entryNamePath === "") continue;

    const entryPath = path.join(
      deploymentDir,
      entry.entryName.split("/").slice(1).join("/"),
    );

    if (entry.entryName.at(-1) === "/") {
      try {
        mkdirSync(entryPath, { recursive: true });
      } catch {}

      continue;
    }

    writeFileSync(entryPath, entry.getData());
  }

  log.info("Deploying services...");

  deployServices(deploymentDir);
}

function verifySignature(req: Request) {
  try {
    const signature = crypto
      .createHmac("sha256", env.WEBHOOK_SECRET)
      .update(JSON.stringify(req.body))
      .digest("hex");
    let trusted = Buffer.from(`sha256=${signature}`, "ascii");
    let untrusted = Buffer.from(
      req.headers.get("x-hub-signature-256")!,
      "ascii",
    );
    return crypto.timingSafeEqual(trusted, untrusted);
  } catch {
    return false;
  }
}

app.post("/webhooks/action-finished", async (c) => {
  if (env.NODE_ENV === "production" && !verifySignature(c.req.raw)) {
    c.status(401);
    return c.json({ message: "Unauthorized" });
  }

  try {
    onActionFinished();
  } catch (e) {
    if (e instanceof Error) {
      log.error({
        ...e,
        message: "Unknown error occured when starting or running deploment",
      });
    }
  }

  c.status(200);
  return c.json({ message: "Acknowledged" });
});

app.get("/health", (c) => {
  c.status(200);
  return c.json({ message: "Service is running" });
});

serve({ ...app, port: env.ORCHESTRATOR_PORT }, (info) => {
  log.info(`Orchestrator running on port ${info.port}`);
});
