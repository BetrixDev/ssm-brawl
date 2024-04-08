import { Hono } from "hono";
import { serve } from "@hono/node-server";
import { Octokit } from "octokit";
import { env } from "env";
import path from "path";
import { existsSync, mkdirSync, writeFileSync } from "fs";
import AdmZip from "adm-zip";
import pm from "picomatch";
import { execa, ExecaChildProcess } from "execa";
import axios from "axios";
import crypto from "crypto";
import { pino } from "pino";
import { LogBody, Logger } from "logger";
import chalk from "chalk";

const appLogger = pino(
  { level: "info" },
  pino.transport({
    target: "@axiomhq/pino",
    options: {
      dataset: env.AXIOM_DATASET,
      token: env.AXIOM_TOKEN,
    },
  })
);

function handleNewLog(data: LogBody) {
  switch (data.level) {
    case "Error":
      console.log(chalk.red(JSON.stringify(data)));
      appLogger.error(data);
      break;

    case "Info":
      console.log(JSON.stringify(data));
      appLogger.info(data);
      break;

    default:
      appLogger.info(data);
      break;
  }
}

const log = new Logger("orchestrator", handleNewLog);

let runningProcessed: { id: string; process: ExecaChildProcess<string> }[] = [];

const API_START_TIMEOUT = 15000;
const DEPLOYMENTS_DIR = path.join(process.cwd(), "..", "deployments");

const app = new Hono();

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

async function getLatestPluginUrl() {
  try {
    const response = await github.rest.repos.getLatestRelease({
      owner: "BetrixDev",
      repo: "ssm-brawl",
    });

    return response.data.assets.find(({ name }) =>
      pm.isMatch(name, "ssmb*.jar")
    )?.browser_download_url;
  } catch {
    return;
  }
}

async function deployServices(deploymentPath: string) {
  const pluginUrl = await getLatestPluginUrl();

  log.info("Downloading plugin jar...", { pluginUrl });

  if (!pluginUrl) {
    log.error("Failed to download plugin jar, aborting deployment");
    return;
  }

  log.info(pluginUrl);

  const pluginBuffer = await axios(pluginUrl, { responseType: "arraybuffer" });
  const pluginPath = path.join(deploymentPath, "server", "plugins", "ssmb.jar");

  writeFileSync(pluginPath, pluginBuffer.data);

  log.info("Installing dependencies...");

  await execa("pnpm install", undefined, { cwd: deploymentPath });

  log.info("Building services...");

  await execa("pnpm build", { cwd: deploymentPath });

  log.info("Killing old process...");

  runningProcessed.filter(({ process }) => {
    process.kill();
    return false;
  });

  log.info("Starting api...");

  const apiProcess = execa("pnpm start --filter=api", {
    cwd: deploymentPath,
    env: process.env,
    stdio: "inherit",
  });

  if (apiProcess.stdout === null) {
    log.error("Failed to start API, aborting deployment");
    return;
  }

  apiProcess.stdout.on("data", (data) => {
    handleNewLog(JSON.parse(data));
  });

  runningProcessed.push({
    id: "api",
    process: apiProcess,
  });

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

  runningProcessed.push({
    id: "server",
    process: execa("pnpm start --filter=server", {
      cwd: deploymentPath,
      stdio: "pipe",
    }),
  });

  // runningProcessed.push({
  //   id: "brawlie",
  //   process: execa("pnpm start --filter=brawlie", {
  //     cwd: deploymentPath,
  //   }),
  // });
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
    Buffer.from(deloymentContentsResponse.data as ArrayBuffer)
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
      pm.isMatch(glob.includes("/") ? path.replace(".", "a") : path, glob)
    );
  }

  const deploymentDir = path.join(
    DEPLOYMENTS_DIR,
    `${Date.now()}-${deploymentId}`
  );

  mkdirSync(deploymentDir);

  for (const entry of entries) {
    const entryNamePath = entry.entryName.split("/").slice(1).join("/");

    if (shouldIgnoreFile(entryNamePath)) continue;

    if (entryNamePath === "") continue;

    const entryPath = path.join(
      deploymentDir,
      entry.entryName.split("/").slice(1).join("/")
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
      "ascii"
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

  onActionFinished();

  c.status(200);
  return c.json({ message: "Acknowledged" });
});

serve({ ...app, port: env.ORCHESTRATOR_PORT }, (info) => {
  log.info(`Orchestrator running on port ${info.port}`);
});
