import { o } from "@/lib/orpc";
import { pluginKvRouter } from "./plugin-kv-router";
import { pluginPlayersRouter } from "./plugin-players-router";

export const pluginRouter = {
  players: o.prefix("/players").router(pluginPlayersRouter),
  kv: o.prefix("/kv").router(pluginKvRouter),
};
