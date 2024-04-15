import { initTRPC, TRPCError } from "@trpc/server";
import { JwtClaims } from "./jwt.js";
import { TRPCPanelMeta } from "trpc-panel";

export type TrpcContext = {
  claims: JwtClaims;
  resHeaders: Headers;
};

export const t = initTRPC.meta<TRPCPanelMeta>().context<TrpcContext>().create();

export const router = t.router;
export const procedure = t.procedure;

export const internalProcedure = t.procedure.use((opts) => {
  if (opts.ctx.claims.source === "user") {
    throw new TRPCError({ code: "FORBIDDEN" });
  }

  return opts.next({
    ctx: { ...opts.ctx },
  });
});

export const userProcedure = t.procedure.use((opts) => {
  if (opts.ctx.claims.source !== "user") {
    throw new TRPCError({ code: "FORBIDDEN" });
  }

  return opts.next({
    ctx: { ...opts.ctx },
  });
});
