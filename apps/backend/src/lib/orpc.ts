import { ORPCError, os } from "@orpc/server";
import { auth } from "./auth";
import type { Context } from "./context";
import { env } from "./env";

export const o = os.$context<Context>();

export const sessionProvider = o
  .$context<{
    session?: any;

    headers: Headers;
  }>()
  .middleware(async ({ context, next }) => {
    context.session ??= await auth?.api.getSession({
      headers: context.headers,
    });

    return next({
      context: {
        session: context.session,
      },
    });
  });

const requireAuth = sessionProvider.concat(
  o
    .$context<{
      session?: any;
    }>()
    .middleware(async ({ context, next }) => {
      if (!context.session?.user) {
        throw new ORPCError("UNAUTHORIZED");
      }

      return next({
        context: {
          session: context.session,
        },
      });
    }),
);

const requirePlugin = o.middleware(async ({ context, next }) => {
  if (
    !context.headers.get("Authorization")?.startsWith("Bearer ") ||
    context.headers.get("Authorization")?.split(" ")[1] !== env.PLUGIN_SECRET_KEY
  ) {
    throw new ORPCError("UNAUTHORIZED");
  }

  return next();
});

export const publicProcedure = o;
export const protectedProcedure = publicProcedure.use(requireAuth);
export const pluginProcedure = publicProcedure.use(requirePlugin);
