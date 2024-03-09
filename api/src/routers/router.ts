import { router } from "../trpc.js";
import { minigameRouter } from "./minigameRouter.js";
import { playerRouter } from "./playerRouter.js";
import { queueRouter } from "./queueRouter.js";

export const appRouter = router({
  minigame: minigameRouter,
  queue: queueRouter,
  player: playerRouter,
});

export type AppRouter = typeof appRouter;
