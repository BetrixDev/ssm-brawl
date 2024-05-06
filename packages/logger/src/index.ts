import type { MiddlewareHandler } from "hono";
import { Logger, type LogBody } from "./Logger.js";

export function middlewareLogger(log: Logger): MiddlewareHandler {
  return async (c, next) => {
    const start = Date.now();

    try {
      const jsonPayload = await c.req.raw.json();
      (c as any).jsonPayload = jsonPayload;

      log.info({
        method: c.req.method,
        path: c.req.path,
        payload: jsonPayload,
      });
    } catch {
      log.info({
        method: c.req.method,
        path: c.req.path,
        payload: null,
      });
    }

    await next();

    try {
      log.info({
        method: c.req.method,
        path: c.req.path,
        status: c.res.status,
        elapsedMs: Date.now() - start,
        //                responseBody: await c.res.json()
      });
    } catch {
      log.info({
        method: c.req.method,
        path: c.req.path,
        status: c.res.status,
        elapsedMs: Date.now() - start,
        responseBody: null,
      });
    }
  };
}

export { Logger, LogBody };
