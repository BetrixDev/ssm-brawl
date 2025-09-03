import { cors } from "@elysiajs/cors";
import { fetchRequestHandler } from "@trpc/server/adapters/fetch";
import { Elysia } from "elysia";
import { appRouter } from "./trpc/routers";

export const app = new Elysia()
  .use(cors())
  .all("/trpc/*", ({ request }) => {
    return fetchRequestHandler({
      endpoint: "/trpc",
      req: request,
      router: appRouter,
      createContext() {
        return {};
      },
    });
  })
  .get("/hc", () => {
    return {
      message: "Server is healthy",
    };
  });

export function createTanstackStartRequestHandler() {
  return async ({ request }: { request: Request }) => {
    // Remove "/api" from the pathname if present
    const url = new URL(request.url);
    if (url.pathname.startsWith("/api")) {
      url.pathname = url.pathname.replace(/^\/api/, "") || "/";
    }
    // Create a new Request with the modified URL
    const newRequest = new Request(url.toString(), request);
    return app.handle(newRequest);
  };
}

export type App = typeof app;
export type AppRouter = typeof appRouter;
