import { createServerFileRoute } from "@tanstack/react-start/server";
import { createTanstackStartRequestHandler } from "backend";

const requestHandler = createTanstackStartRequestHandler();

export const ServerRoute = createServerFileRoute("/api/$").methods({
  GET: requestHandler,
  POST: requestHandler,
  PUT: requestHandler,
  DELETE: requestHandler,
  PATCH: requestHandler,
  OPTIONS: requestHandler,
  HEAD: requestHandler,
});
