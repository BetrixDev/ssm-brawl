import { checkout, polar, portal } from "@polar-sh/better-auth";
import { betterAuth, type BetterAuthOptions } from "better-auth";
import { drizzleAdapter } from "better-auth/adapters/drizzle";
import { username } from "better-auth/plugins";
import { db } from "../db/db";
import * as schema from "../db/schema";
import { env } from "./env";
import { polarClient } from "./payments";

export const auth = betterAuth<BetterAuthOptions>({
  database: drizzleAdapter(db, {
    provider: "sqlite",
    usePlural: true,
    schema: schema,
  }),
  trustedOrigins: env.CORS_ORIGIN ? [env.CORS_ORIGIN] : [],
  emailAndPassword: {
    enabled: true,
  },
  // session: {
  //   cookieCache: {
  //     enabled: true,
  //     maxAge: 60,
  //   },
  // },
  secret: env.BETTER_AUTH_SECRET,
  baseURL: env.BETTER_AUTH_URL,
  advanced: {
    defaultCookieAttributes: {
      sameSite: "none",
      secure: true,
      httpOnly: true,
    },
  },
  plugins: [
    username(),
    polar({
      client: polarClient,
      createCustomerOnSignUp: true,
      enableCustomerPortal: true,
      use: [
        checkout({
          products: [],
          successUrl: env.POLAR_SUCCESS_URL,
          authenticatedUsersOnly: true,
        }),
        portal(),
      ],
    }),
  ],
});
