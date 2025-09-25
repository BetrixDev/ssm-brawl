import type { RouterUtils } from "@orpc/react-query";
import { createContext, use } from "react";
import type { AppRouterClient } from "../../../backend/src/routers/index";

type RpcReactUtils = RouterUtils<AppRouterClient>;

export const RpcContext = createContext<RpcReactUtils | undefined>(undefined);

export function useRpc(): RpcReactUtils {
  const rpc = use(RpcContext);

  if (!rpc) {
    throw new Error("ORPCContext is not set up properly");
  }

  return rpc;
}
