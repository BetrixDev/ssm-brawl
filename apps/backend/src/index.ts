import { cors } from "@elysiajs/cors";
import { OpenAPIHandler } from "@orpc/openapi/fetch";
import { OpenAPIReferencePlugin } from "@orpc/openapi/plugins";
import { onError } from "@orpc/server";
import { RPCHandler } from "@orpc/server/fetch";
import { ZodToJsonSchemaConverter } from "@orpc/zod/zod4";
import { Elysia } from "elysia";
import logixlysia from "logixlysia";
import { auth } from "./lib/auth";
import { createContext } from "./lib/context";
import { env } from "./lib/env";
import { appRouter } from "./routers/index";

new Elysia({ name: "Super Smash Mobs Brawl Api" })
  .use(
    logixlysia({
      config: {
        ip: true,
        startupMessageFormat: "simple",
        showStartupMessage: true,
      },
    }),
  )
  .use(
    cors(
      env.CORS_ORIGIN
        ? {
            origin: env.CORS_ORIGIN,
            methods: ["GET", "POST", "OPTIONS"],
            allowedHeaders: ["Content-Type", "Authorization"],
            credentials: true,
          }
        : undefined,
    ),
  )
  .mount("/auth", auth.handler)
  .all(
    "/*",
    async ({ request }) => {
      const context = await createContext({ request });

      const rpcResult = await rpcHandler.handle(request, {
        prefix: "/rpc",
        context: context,
      });

      if (rpcResult.matched) {
        return rpcResult.response;
      }

      const apiResult = await apiHandler.handle(request, {
        prefix: "/",
        context: context,
      });

      if (apiResult.matched) {
        return apiResult.response;
      }

      return new Response("Not Found", { status: 404 });
    },
    {
      parse: "none",
    },
  )
  .get("/", () => {
    return "OK";
  })
  .listen(3000);

export const apiHandler = new OpenAPIHandler(appRouter, {
  plugins: [
    new OpenAPIReferencePlugin({
      schemaConverters: [new ZodToJsonSchemaConverter()],
    }),
  ],
  interceptors: [
    onError((error) => {
      console.error(error);
    }),
  ],
});

export const rpcHandler = new RPCHandler(appRouter, {
  interceptors: [
    onError((error) => {
      console.error(error);
    }),
  ],
});
