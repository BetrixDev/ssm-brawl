import { router } from "../trpc.js";
import { friendshipRouter } from "./friendshipRouter.js";
import { kitsRouter } from "./kitRouter.js";
import { mapRouter } from "./mapRouter.js";
import { minigameRouter } from "./minigameRouter.js";
import { partyRouter } from "./partyRouter.js";
import { playerRouter } from "./playerRouter.js";
import { pmRouter } from "./pmRouter.js";
import { queueRouter } from "./queueRouter.js";
import { serverRouter } from "./serverRouter.js";

export const appRouter = router({
  minigame: minigameRouter,
  queue: queueRouter,
  player: playerRouter,
  friendships: friendshipRouter,
  pm: pmRouter,
  kits: kitsRouter,
  parties: partyRouter,
  maps: mapRouter,
  server: serverRouter,
});

export type AppRouter = typeof appRouter;
