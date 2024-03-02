import { QueryClient } from "@tanstack/query-core";

const queryClient = new QueryClient();

export function useQueryClient() {
  return queryClient;
}
