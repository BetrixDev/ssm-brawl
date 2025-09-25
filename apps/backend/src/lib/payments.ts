import { Polar } from "@polar-sh/sdk";
import { env } from "cloudflare:workers";
import { z } from "zod";

const polarServerSchema = z.enum(["production", "sandbox"]);

export const polarClient = new Polar({
  accessToken: env.POLAR_ACCESS_TOKEN,
  server: polarServerSchema.parse(env.POLAR_SERVER),
});
