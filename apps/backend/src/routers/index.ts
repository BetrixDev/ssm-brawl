import { db } from "@/db";
import type { RouterClient } from "@orpc/server";
import { env } from "cloudflare:workers";
import { publicProcedure } from "../lib/orpc";
import { pluginRouter } from "./plugin";

export const appRouter = {
  healthCheck: publicProcedure.route({ method: "GET", path: "/health-check" }).handler(async () => {
    const dbCheckStart = performance.now();

    await db.$client.query("SELECT 1");

    const dbCheckEnd = performance.now();

    const dbCheckDuration = dbCheckEnd - dbCheckStart;

    const kvCheckStart = performance.now();
    await env.KV.put("test", "test");
    await env.KV.get("test");
    const kvCheckEnd = performance.now();
    const kvCheckDuration = kvCheckEnd - kvCheckStart;

    return {
      status: "OK",
      dbCheckDurationMs: dbCheckDuration,
      kvCheckDurationMs: kvCheckDuration,
    };
  }),
  plugin: pluginRouter,
};

export type AppRouter = typeof appRouter;
export type AppRouterClient = RouterClient<typeof appRouter>;
