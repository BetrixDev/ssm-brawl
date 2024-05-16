import { QueryClient } from "@tanstack/query-core";
import { customAlphabet } from "nanoid";

export const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 1000 * 60 * 5 } },
});

const numberNanoid = customAlphabet("1234567890");

export function useRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

export function useRandomId(length: number) {
  return numberNanoid(length);
}

export function get(obj: any, path: string, fallback: any = undefined) {
  if (!obj || !path) return fallback;
  const paths = Array.isArray(path) ? path : path.split(".");
  let results = obj;
  let i = 0;

  while (i < paths.length && results !== undefined && results !== null) {
    results = results[paths[i]];
    i++;
  }

  if (i === paths.length) {
    return results !== undefined ? results : fallback;
  }

  return results !== undefined && results !== null ? results : fallback;
}

export function sortStrings(list: string[]) {
  return list.sort((a, b) => a.localeCompare(b));
}
