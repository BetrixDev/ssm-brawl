import { router } from "../trpc.js";
import { friendshipRouter } from "./friendshipRouter.js";
import { kitsRouter } from "./kitsRouter.js";
import { langRouter } from "./langRouter.js";
import { minigameRouter } from "./minigameRouter.js";
import { playerRouter } from "./playerRouter.js";
import { pmRouter } from "./pmRouter.js";
import { queueRouter } from "./queueRouter.js";

export const appRouter = router({
  minigame: minigameRouter,
  queue: queueRouter,
  player: playerRouter,
  lang: langRouter,
  friendships: friendshipRouter,
  pm: pmRouter,
  kits: kitsRouter,
});

export type AppRouter = typeof appRouter;
