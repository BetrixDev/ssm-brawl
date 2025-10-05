import { o } from "@/lib/orpc";
import { pluginPlayersRouter } from "./plugin-players-router";

export const pluginRouter = {
  players: o.prefix("/players").router(pluginPlayersRouter),
};
