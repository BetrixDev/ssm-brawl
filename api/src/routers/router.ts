import { router } from "../trpc.js";
import { langRouter } from "./langRouter.js";
import { minigameRouter } from "./minigameRouter.js";
import { playerRouter } from "./playerRouter.js";
import { queueRouter } from "./queueRouter.js";

export const appRouter = router({
  minigame: minigameRouter,
  queue: queueRouter,
  player: playerRouter,
  lang: langRouter,
});

export type AppRouter = typeof appRouter;
