import axios from "axios";
import { env } from "env/api";
import { createTRPCProxyClient, httpBatchLink } from "@trpc/client";

const BASE_API_URL = `${env.API_PROTOCOL}://${env.API_HOST}:${env.API_PORT}`;

export const trpcClient = createTRPCProxyClient({
  links: [
    httpBatchLink({
      url: `${BASE_API_URL}/trpc`,
      headers: async () => {
        const tokenResponse = await axios.post<string>(
          `${BASE_API_URL}/generateToken/brawlie`,
          {},
          {
            headers: {
              Secret: env.API_TOKEN_SECRET,
            },
          }
        );

        return {
          Cookie: `token=${tokenResponse.data}`,
        };
      },
    }),
  ],
});
