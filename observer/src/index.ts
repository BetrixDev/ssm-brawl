import { execaCommand } from "execa";
import { env } from "env";
import { pino } from "pino";
import { schedule } from "node-cron";
import { listRunningProcesses } from "proc";
import pidusage from "pidusage";

const logger = pino(
  { level: "info" },
  pino.transport({
    target: "@axiomhq/pino",
    options: {
      dataset: env.AXIOM_DATASET,
      token: env.AXIOM_TOKEN,
    },
  })
);

const logsWatcher = execaCommand("pm2 logs --raw", {
  stdout: "pipe",
});

if (logsWatcher.stdout === null) {
  throw new Error("Failed to start log watcher child process");
}

logsWatcher.stdout.on("data", (buf: Buffer) => {
  const stringLog = buf.toString();

  try {
    logger.info(JSON.parse(stringLog));
  } catch {
    logger.info(stringLog);
  }
});

schedule("*/5 * * * *", async () => {
  const runningProcesses = await listRunningProcesses();

  for (const { pid, name } of runningProcesses) {
    try {
      if (pid) {
        const stats = await pidusage(pid);

        logger.info({
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
      logger.warn({
        service: "observer",
        message: `Unable to get pidusage for process with id: ${pid} and name: ${name}`,
        pid,
        name,
      });
    }
  }
});
