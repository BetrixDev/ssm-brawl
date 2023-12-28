import path from "path";
import { existsSync, readdirSync, rmSync } from "fs";

const multiverseWorldsYmlPath = path.join(
  process.cwd(),
  "plugins/Multiverse-Core/worlds.yml"
);

const citizensSavesYmlPath = path.join(
  process.cwd(),
  "plugins/Citizens/saves.yml"
);

const worldsToDelete = readdirSync(process.cwd())
  .filter((p) => p.startsWith("ssmb_world_"))
  .map((p) => path.join(process.cwd(), p));

if (existsSync(multiverseWorldsYmlPath)) {
  rmSync(multiverseWorldsYmlPath);
}

if (existsSync(citizensSavesYmlPath)) {
  rmSync(citizensSavesYmlPath);
}

worldsToDelete.forEach((path) => {
  rmSync(path, { force: true, recursive: true });
});
