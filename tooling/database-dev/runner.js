import { spawn } from "child_process";
import path from "node:path";

function getDockerComposeCommand() {
  try {
    execSync("docker compose version", { stdio: "ignore" });
    return ["docker", "compose"];
  } catch {
    return ["docker-compose"];
  }
}

function runDockerCompose() {
  const [command, ...baseArgs] = getDockerComposeCommand();
  const args = [...baseArgs, "up", "-d"];

  const dockerCompose = spawn(command, args, {
    cwd: path.resolve(process.cwd(), "..", ".."),
  });

  dockerCompose.stdout.on("data", (data) => {
    console.log(`${data}`);
  });

  dockerCompose.stderr.on("data", (data) => {
    console.error(`${data}`);
  });

  dockerCompose.on("close", (code) => {
    console.log(`Process exited with code ${code}`);
  });
}

runDockerCompose();
