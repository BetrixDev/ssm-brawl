import { router } from "../trpc.js";
import { minigameRouter } from "./minigameRouter.js";
import { queueRouter } from "./queueRouter.js";

export const appRouter = router({
  minigame: minigameRouter,
  queue: queueRouter,
});

export type AppRouter = typeof appRouter;
