import { db } from "@/db/db";
import { ORPCError, type RouterClient } from "@orpc/server";
import { o, publicProcedure } from "../lib/orpc";
import { playersRouter } from "./players/players-router";
import { pluginRouter } from "./plugin/plugin-router";

export const appRouter = {
  healthCheck: publicProcedure.route({ method: "GET", path: "/hc" }).handler(async () => {
    const dbCheckStart = performance.now();

    try {
      db.execute("select 1");
    } catch (error) {
      console.error(error);

      throw new ORPCError("INTERNAL_SERVER_ERROR", {
        status: 500,
        message: "Database connection failed",
        cause: error,
      });
    }

    const dbCheckDuration = performance.now() - dbCheckStart;

    return {
      status: "OK",
      dbCheckDurationMs: dbCheckDuration,
      timestamp: new Date().toISOString(),
    };
  }),
  plugin: o.prefix("/plugin").router(pluginRouter),
  players: o.prefix("/players").router(playersRouter),
};

export type AppRouter = typeof appRouter;
export type AppRouterClient = RouterClient<typeof appRouter>;
