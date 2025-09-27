import type { RouterClient } from "@orpc/server";
import { dbProvider, kvProvider, o, publicProcedure } from "../lib/orpc";
import { pluginRouter } from "./plugin/plugin-router";

export const appRouter = {
  healthCheck: publicProcedure
    .use(dbProvider)
    .use(kvProvider)
    .route({ method: "GET", path: "/health-check" })
    .handler(async ({ context }) => {
      const dbCheckStart = performance.now();

      try {
        await context.db.execute("select 1");
      } catch (error) {
        console.error(error);
        return {
          status: "ERROR",
          message: "Database connection failed",
        };
      }

      const dbCheckDuration = performance.now() - dbCheckStart;

      const kvCheckStart = performance.now();

      try {
        await context.kv.set("test", "test");
        await context.kv.get("test");
      } catch (error) {
        console.error(error);
        return {
          status: "ERROR",
          message: "KV connection failed",
        };
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
};

export type AppRouter = typeof appRouter;
export type AppRouterClient = RouterClient<typeof appRouter>;
