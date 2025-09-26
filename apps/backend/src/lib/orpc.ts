import { ORPCError, os } from "@orpc/server";
import { env } from "cloudflare:workers";
import type { Context } from "./context";

export const o = os.$context<Context>();

const requireAuth = o.middleware(async ({ context, next }) => {
  if (!context.session?.user) {
    throw new ORPCError("UNAUTHORIZED");
  }
  return next({
    context: {
      session: context.session,
    },
  });
});

const requirePlugin = o.middleware(async ({ context, next }) => {
  if (
    !context.headers.get("Authorization")?.startsWith("Bearer ") ||
    context.headers.get("Authorization")?.split(" ")[1] !== env.PLUGIN_SECRET
  ) {
    throw new ORPCError("UNAUTHORIZED");
  }

  return next();
});

export const publicProcedure = o;
export const protectedProcedure = publicProcedure.use(requireAuth);
export const pluginProcedure = publicProcedure.use(requirePlugin);
