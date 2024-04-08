import type { MiddlewareHandler } from "hono";
import { Logger, type LogBody } from "./Logger.js";

export function middlewareLogger(log: Logger): MiddlewareHandler {
  return async (c, next) => {
    const start = Date.now();

    log.info({
      method: c.req.method,
      path: c.req.path,
      payload: c.req.raw.body,
    });

    await next();

    log.info({
      method: c.req.method,
      path: c.req.path,
      status: c.res.status,
      elapsedMs: Date.now() - start,
    });
  };
}

export { Logger, LogBody };
