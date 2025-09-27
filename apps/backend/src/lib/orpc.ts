import { getDb, type Database } from "@/db";
import { getAuth } from "@/lib/auth";
import { ORPCError, os } from "@orpc/server";
import { env } from "cloudflare:workers";
import type { Context } from "./context";

export const o = os.$context<Context>();

export const dbProvider = o.$context<{ db?: Database }>().middleware(async ({ context, next }) => {
  context.db ??= await getDb();

  return next({
    context: {
      db: context.db,
    },
  });
});

export const authProvider = o
  .$context<{ auth?: ReturnType<typeof getAuth>; db?: Database }>()
  .middleware(async ({ context, next }) => {
    context.db ??= await getDb();
    context.auth ??= getAuth(context.db);

    return next({
      context: {
        auth: context.auth,
      },
    });
  });

export const sessionProvider = o
  .$context<{
    session?: any;
    auth?: ReturnType<typeof getAuth>;
    db?: Database;
    headers: Headers;
  }>()
  .middleware(async ({ context, next }) => {
    context.db ??= await getDb();
    context.auth ??= getAuth(context.db);

    context.session ??= await context.auth?.api.getSession({
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
