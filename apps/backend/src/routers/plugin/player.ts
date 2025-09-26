import { pluginProcedure } from "@/lib/orpc";

export const pluginPlayersRouter = {
    getPlayerDocument: pluginProcedure.route({ method: "GET", path: "/player/{uuid}/document" }).handler(async ({ context }) => {}),
    savePlayerDocument: pluginProcedure.route({ method: "PUT", path: "/player/{uuid}/document" }).handler(async ({ context }) => {
        return {
            message: "Player document saved",
        }
    }),
}