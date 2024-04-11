import axios from "axios";
import { env } from "env/api";
import { execa, execaCommand, execaCommandSync } from "execa";
import { Logger } from "logger";

const log = new Logger("server_starter");

const API_CONNECTION_TIMEOUT = 30000;

let isApiConnected = false;
let timeStarted = Date.now();

log.info("Waiting for API connection...");

while (!isApiConnected) {
  if (Date.now() - timeStarted > API_CONNECTION_TIMEOUT) {
    log.error("API connection timeout");
    throw new Error("API connection timeout");
  }

  try {
    const response = await axios(
      `${env.API_PROTOCOL}://${env.API_HOST}:${env.API_PORT}/health`
    );

    if (response.status === 200) {
      isApiConnected = true;
    }
  } catch {
    log.error({ message: "API connection failed, retrying..." });

    await new Promise((resolve) => setTimeout(resolve, 500));
  }
}

log.info("API connection established");
log.info("Starting server...");

await execa("java -Xmx8G -jar pufferfish.jar -nogui", {
  stdio: "inherit",
  env: process.env,
});

serverProcess.on("exit", () => {
  log.info("Server process exited");
  process.exit(0);
});

process.on("beforeExit", () => {
  try {
    serverProcess.kill();
  } catch {}
});
