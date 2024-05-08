import { TRPCError } from "@trpc/server";
import { kv } from "../kv.js";
import { internalProcedure, router, t } from "../trpc.js";
import { appRouter } from "./router.js";

export const serverRouter = router({
  beginShutdown: internalProcedure.mutation(async ({ ctx }) => {
    const isServerShuttingDown = await kv.getItem("isShuttingDown");

    if (isServerShuttingDown) {
      throw new TRPCError({ code: "CONFLICT" });
    }

    await kv.setItem("isShuttingDown", true);

    const internalCaller = t.createCallerFactory(appRouter)(ctx);
    await internalCaller.queue.flushQueue();
  }),
});
