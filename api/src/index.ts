import fastify from "fastify";
import cors from "@fastify/cors";
import {
  fastifyTRPCPlugin,
  FastifyTRPCPluginOptions,
} from "@trpc/server/adapters/fastify";
import { appRouter, AppRouter } from "./routers/router.js";
import { get } from "lodash-es";
import { t } from "./trpc.js";
import { renderTrpcPanel } from "trpc-panel";
import { TRPCError } from "@trpc/server";
import { getHTTPStatusCodeFromError } from "@trpc/server/http";
import bearerAuth from "@fastify/bearer-auth";
import { env } from "env";

const server = fastify();
server.register(cors);
server.register(bearerAuth, {
  keys: [],
  auth: (key) => key === env.API_AUTH_TOKEN,
});

server.register(fastifyTRPCPlugin, {
  prefix: "/trpc",
  trpcOptions: {
    router: appRouter,
  } satisfies FastifyTRPCPluginOptions<AppRouter>["trpcOptions"],
});

const caller = t.createCallerFactory(appRouter)({});

server.all("/api/*", async (req, res) => {
  const paths = req.url.replace("/api/", "").replace("/", ".");

  const routerProcedure = await get(caller, paths);

  if (routerProcedure === undefined) {
    return res.status(404);
  }

  let routerResponse: unknown;

  try {
    routerResponse = await routerProcedure(req.body);
  } catch (e: unknown) {
    if (e instanceof TRPCError) {
      const statusCode = getHTTPStatusCodeFromError(e);
      return res.status(statusCode).send(e);
    } else {
      res.status(500).send(e);
    }
  }

  return res.status(200).send(routerResponse);
});

server.get("/panel", (_, res) => {
  return res
    .type("text/html")
    .send(renderTrpcPanel(appRouter, { url: "http://localhost:3000/trpc" }));
});

async function main() {
  await server.listen({ port: 3000 });

  console.log("listening");
}

main();
