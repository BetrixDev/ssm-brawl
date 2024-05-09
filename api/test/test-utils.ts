import { appRouter } from "../src/routers/router.js";
import { t } from "../src/trpc.js";

export function createInternalCaller() {
  return t.createCallerFactory(appRouter)({
    resHeaders: new Headers(),
    claims: { iat: Date.now(), source: "plugin" },
  });
}
