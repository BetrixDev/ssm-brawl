import type { MiddlewareHandler } from "hono";
import { LOG_LEVELS, Logger, type LogBody } from "./Logger.js";

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

export function handleStdIOLog(
  log: Logger,
  service: string,
  level: keyof typeof LOG_LEVELS,
  handleNewLog: (payload: LogBody) => void
) {
  return (data: Buffer) => {
    const logString = data.toString();

    try {
      handleNewLog({
        ...JSON.parse(logString),
        level: LOG_LEVELS[level],
        service,
      });
    } catch {
      handleNewLog({
        level: LOG_LEVELS[level],
        service,
        message: logString,
      });
    }
  };
}

export { Logger, LogBody };
