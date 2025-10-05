import { db } from "@/db/db";
import { getKv, setKv } from "@/db/queries/kv";
import { ORPCError, type RouterClient } from "@orpc/server";
import { o, publicProcedure } from "../lib/orpc";
import { playersRouter } from "./players/players-router";
import { pluginRouter } from "./plugin/plugin-router";

export const appRouter = {
  healthCheck: publicProcedure.route({ method: "GET", path: "/hc" }).handler(async () => {
    const dbCheckStart = performance.now();

    try {
      db.$client.run("select 1");
    } catch (error) {
      console.error(error);

      throw new ORPCError("INTERNAL_SERVER_ERROR", {
        status: 500,
        message: "Database connection failed",
        cause: error,
      });
    }

    const dbCheckDuration = performance.now() - dbCheckStart;

    const kvCheckStart = performance.now();

    try {
      await setKv("test", "test");
      await getKv("test");
    } catch (error) {
      console.error(error);

      throw new ORPCError("INTERNAL_SERVER_ERROR", {
        status: 500,
        message: "KV database failed",
        cause: error,
      });
    }

    const kvCheckDuration = performance.now() - kvCheckStart;

    return {
      status: "OK",
      dbCheckDurationMs: dbCheckDuration,
      kvCheckDurationMs: kvCheckDuration,
      timestamp: new Date().toISOString(),
    };
  }),
  plugin: o.prefix("/plugin").router(pluginRouter),
  players: o.prefix("/players").router(playersRouter),
};

export type AppRouter = typeof appRouter;
export type AppRouterClient = RouterClient<typeof appRouter>;
