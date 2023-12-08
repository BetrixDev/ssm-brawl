import { env } from "./env";

export function isAuthedForRequest(request: Request) {
  return (
    request.headers.get("Authorization") === `Bearer ${env.API_AUTH_TOKEN}`
  );
}

export function getRandomElement<T extends Array<unknown>>(arr: T) {
  const randomIndex = Math.floor(Math.random() * arr.length);
  return arr[randomIndex] as T[number];
}
