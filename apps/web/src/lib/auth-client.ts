import { polarClient } from "@polar-sh/better-auth";
import { createAuthClient } from "better-auth/client";
import { usernameClient } from "better-auth/client/plugins";

export const authClient = createAuthClient({
  baseURL: import.meta.env.DEV ? "http://localhost:3000/auth" : "https://api.ssmbrawl.com/auth",
  plugins: [usernameClient(), polarClient()],
});
