import { execaCommand } from "execa";
import { env } from "env";
import { schedule } from "node-cron";
import { listRunningProcesses } from "proc";
import pidusage from "pidusage";
import { Axiom } from "@axiomhq/js";

if (env.AXIOM_TOKEN === undefined) {
  throw new Error("AXIOM_TOKEN not defined in env");
}

if (env.AXIOM_DATASET === undefined) {
  throw new Error("AXIOM_DATASET not defined in env");
}

const dataset = env.AXIOM_DATASET;

const axiom = new Axiom({
  token: env.AXIOM_TOKEN,
  orgId: env.AXIOM_ORG_ID,
});

const logsWatcher = execaCommand("pm2 logs --raw", {
  stdout: "pipe",
});

if (logsWatcher.stdout === null) {
  throw new Error("Failed to start log watcher child process");
}

logsWatcher.stdout.on("data", (buf: Buffer) => {
  const stringLog = buf.toString();

  try {
    axiom.ingest(dataset, JSON.parse(stringLog));
  } catch {
    axiom.ingest(dataset, { message: stringLog });
  }
});

schedule("*/5 * * * *", async () => {
  const runningProcesses = await listRunningProcesses();

  for (const { pid, name } of runningProcesses) {
    try {
      if (pid) {
        const stats = await pidusage(pid);

        axiom.ingest(dataset, {
          service: "observer",
          message: "New process stats",
          process: {
            name,
            cpi: stats.cpu,
            mem: stats.memory,
          },
        });
      }
    } catch {
      axiom.ingest(dataset, {
        service: "observer",
        message: `Unable to get pidusage for process with id: ${pid} and name: ${name}`,
        pid,
        name,
      });
    }
  }
});
