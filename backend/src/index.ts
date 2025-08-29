import { cors } from "@elysiajs/cors";
import { node } from "@elysiajs/node";
import { fetchRequestHandler } from "@trpc/server/adapters/fetch";
import { Elysia } from "elysia";
import { appRouter } from "./trpc/routers";

export const app = new Elysia({ adapter: node() })
  .use(cors())
  .get("/hc", () => {
    return {
      message: "Server is healthy",
    };
  })
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
  .listen(1337, ({ hostname, port }) => {
    if (process.env.NODE_ENV !== "test") {
      console.log(`Backend is running at ${hostname}:${port}`);
    }
  });

export type AppRouter = typeof appRouter;
