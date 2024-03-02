import { env } from "env";

export default defineEventHandler((event) => {
  const authHeader = getRequestHeader(event, "Authorization");

  if (authHeader !== env.API_AUTH_TOKEN) {
    setResponseStatus(event, 403);
    return;
  }
});
