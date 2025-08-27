import { cors } from "@elysiajs/cors";
import { fetchRequestHandler } from "@trpc/server/adapters/fetch";
import { Elysia } from "elysia";
import { appRouter } from "./trpc/routers";

const app = new Elysia()
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
  });

if (process.env.STANDALONE === "true") {
  app.listen(process.env.PORT ?? 1337);

  if (process.env.NODE_ENV !== "test") {
    console.log(`Backend is running at ${app.server?.hostname}:${app.server?.port}`);
  }
}

export default app;

export type AppRouter = typeof appRouter;
