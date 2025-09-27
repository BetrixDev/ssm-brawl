import { o } from "@/lib/orpc";
import { pluginPlayersRouter } from "./plugin-players-router";
import { pluginKvRouter } from "./plugin-kv-router";

export const pluginRouter = {
    players: o.prefix("/players").router(pluginPlayersRouter),
    kv: o.prefix("/kv").router(pluginKvRouter),
}