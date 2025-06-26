import fs from "node:fs";
import path from "node:path";
import { $ } from "bun";

const pluginPath = path.join(
  process.cwd(),
  "..",
  "server",
  "plugins",
  "super-smash-mobs-brawl.jar"
);

console.log("Removing build directory");

fs.rmSync("build", { recursive: true, force: true });

console.log("Removing plugin file");

fs.rmSync(pluginPath, { force: true });

console.log("Building plugin");

console.time("Plugin built");

try {
  await $`./gradlew shadowJar`.quiet();
} catch (error) {
  console.error("Error building plugin:", error);
  process.exit(1);
}

console.timeEnd("Plugin built");

console.log("Copying plugin file");

fs.copyFileSync("build/libs/super-smash-mobs-brawl-0.1.0-all.jar", pluginPath);

console.log("Starting server");

// https://docs.papermc.io/paper/aikars-flags/
await $`java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar server.jar --nogui > out.log`.cwd(
  "../server"
);

console.log("Server stopped");
