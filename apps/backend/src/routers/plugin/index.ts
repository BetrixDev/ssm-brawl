import { o } from "@/lib/orpc";
import { pluginPlayersRouter } from "./player";

export const pluginRouter = {
    players: o.prefix("/players").router(pluginPlayersRouter),
}