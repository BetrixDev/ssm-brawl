import { createORPCClient } from "@orpc/client";
import { RPCLink } from "@orpc/client/fetch";
import { createORPCReactQueryUtils } from "@orpc/react-query";
import { QueryClient } from "@tanstack/react-query";
import { createRouter as createTanStackRouter } from "@tanstack/react-router";
import { routerWithQueryClient } from "@tanstack/react-router-with-query";
import type { AppRouterClient } from "../../backend/src/routers";
import Loader from "./components/loader";
import { RpcContext } from "./contexts/rpc-context";
import "./index.css";
import { routeTree } from "./routeTree.gen";

export function createRouter() {
  const queryClient: QueryClient = new QueryClient();

  const link = new RPCLink({
    url: `${import.meta.env.VITE_SERVER_URL}/rpc`,
    fetch(url, options) {
      return fetch(url, {
        ...options,
        credentials: "include",
      });
    },
  });

  const rpcClient: AppRouterClient = createORPCClient(link);
  const rpc = createORPCReactQueryUtils(rpcClient);

  const router = routerWithQueryClient(
    createTanStackRouter({
      routeTree,
      defaultPreload: "intent",
      defaultPendingComponent: () => <Loader />,
      defaultNotFoundComponent: () => <div>Not Found</div>,
      context: { queryClient, rpc },
      Wrap: ({ children }) => <RpcContext.Provider value={rpc}>{children}</RpcContext.Provider>,
    }),
    queryClient,
  );
  return router;
}

declare module "@tanstack/react-router" {
  interface Register {
    router: ReturnType<typeof createRouter>;
  }
}
