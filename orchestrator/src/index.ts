import { Hono } from "hono";
import { serve } from "@hono/node-server";
import { Octokit } from "octokit";
import { env } from "env";
import path from "path";
import { existsSync, mkdirSync, writeFileSync } from "fs";
import AdmZip from "adm-zip";
import pm from "picomatch";
import { execa, ExecaChildProcess } from "execa";

let subprocess: ExecaChildProcess<string> | undefined;

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

async function deployServices(deploymentPath: string) {
  console.log("Installing dependencies...");

  await execa("pnpm install", undefined, { cwd: deploymentPath });

  console.log("Building services...");

  await execa("pnpm build", { cwd: deploymentPath });

  console.log("Killing old process...");

  if (subprocess) {
    subprocess.kill();
  }

  console.log("Starting services...");

  subprocess = execa("pnpm start", {
    cwd: deploymentPath,
    env: process.env,
  });

  subprocess?.pipeAll?.(process.stdout);

  subprocess.on("disconnect", () => {
    console.log("Subprocess disconnected");
  });

  subprocess.on("error", (err) => {
    console.error(err);
  });

  subprocess.on("close", (code) => {
    console.log(`Subprocess exited with code ${code}`);
  });

  subprocess.on("message", (msg) => {
    console.log(`Message from subprocess: ${msg.toString()}`);
  });
}

async function onActionFinished() {
  console.log("Checking for failed or running actions...");

  const isFailedOrRunningActions = await checkForFailedOrRunningActions();

  if (isFailedOrRunningActions) {
    console.log("Failed or running actions found, aborting deployment");
    return;
  }

  console.log("No failed or running actions found, proceeding with deployment");
  console.log("Downloading deployment contents...");

  const deploymentId = await getLastCommitSha();
  const deloymentContentsResponse =
    await github.rest.repos.downloadZipballArchive({
      owner: "BetrixDev",
      repo: "ssm-brawl",
      ref: "main",
    });

  console.log("Unzipping deployment contents...");

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

  console.log("Deploying services...");

  deployServices(deploymentDir);
}

app.post("/webhooks/action-finished", async (c) => {
  onActionFinished();

  c.status(200);
  return c.json({ message: "Acknowledged" });
});

serve({ ...app, port: env.ORCHESTRATOR_PORT }, (info) => {
  console.log(`Orchestrator running on port ${info.port}`);
});
