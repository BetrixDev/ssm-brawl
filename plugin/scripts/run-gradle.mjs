import { spawn } from "node:child_process";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const filePath = fileURLToPath(import.meta.url);
const scriptDir = dirname(filePath);
const projectRoot = resolve(scriptDir, "..");

const isWindows = process.platform === "win32";
const gradleCommand = isWindows ? "gradlew.bat" : "./gradlew";
const gradleArgs = process.argv.slice(2);

const gradleProcess = spawn(gradleCommand, gradleArgs, {
  cwd: projectRoot,
  stdio: "inherit",
  shell: isWindows,
});

const terminate = () => {
  if (gradleProcess.exitCode === null) {
    gradleProcess.kill();
  }
};

["SIGINT", "SIGTERM", "SIGQUIT", "SIGUSR2"].forEach((signal) => {
  process.once(signal, terminate);
});

process.once("exit", terminate);

gradleProcess.on("error", (error) => {
  console.error("[run-gradle]", error.message);
  process.exitCode = 1;
});

gradleProcess.on("close", (code) => {
  process.exit(code ?? 1);
});
