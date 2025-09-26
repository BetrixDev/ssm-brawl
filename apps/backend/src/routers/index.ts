import { db } from "@/db";
import type { RouterClient } from "@orpc/server";
import { publicProcedure } from "../lib/orpc";
import { pluginRouter } from "./plugin";

export const appRouter = {
  healthCheck: publicProcedure.route({ method: "GET", path: "/health-check" }).handler(async () => {
    const dbCheckStart = performance.now();

    await db.$client.query("SELECT 1");

    const dbCheckEnd = performance.now();

    const dbCheckDuration = dbCheckEnd - dbCheckStart;

    return {
      status: "OK",
      dbCheckDurationMs: dbCheckDuration,
    };
  }),
  plugin: pluginRouter,
};

export type AppRouter = typeof appRouter;
export type AppRouterClient = RouterClient<typeof appRouter>;
