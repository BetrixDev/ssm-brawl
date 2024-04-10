import jwt from "jsonwebtoken";
import { env } from "env/api";
import { z } from "zod";

export const BackendSource = z.enum(["plugin", "web", "brawlie"]);
export type BackendSource = z.infer<typeof BackendSource>;

// Might also become valuable to include the uuid of the player
//  who is reponsible so triggering the backend request

const InternalClaims = z.object({ source: BackendSource });
type InternalClaims = z.infer<typeof InternalClaims>;

const UserClaims = z.object({ uuid: z.string(), source: z.literal("user") });
type UserClaims = z.infer<typeof UserClaims>;

const JwtClaims = z
  .object({ iat: z.number(), exp: z.number().optional() })
  .and(z.union([InternalClaims, UserClaims]));

export type JwtClaims = z.infer<typeof JwtClaims>;

export async function decodeTokenFromHeaders(headers: Headers) {
  try {
    const cookieToken = headers.get("Cookie")?.split(" ")?.at(1);
    const authToken = headers.get("Authorization")?.split(" ")?.at(1);

    const encodedToken = cookieToken ?? authToken;

    if (!encodedToken) return;

    const decodedToken = JwtClaims.parse(
      jwt.verify(encodedToken, env.JWT_PRIVATE_KEY)
    );

    // explicitly check for source and where the token came from to prevent tomfoolery

    if (decodedToken.source === "user" && cookieToken !== undefined) {
      return decodedToken;
    }

    if (decodedToken.source !== "user" && authToken !== undefined) {
      return decodedToken;
    }
  } catch {
    return;
  }
}

export async function generateBackendToken(source: BackendSource) {
  const claims: JwtClaims = { source, iat: Date.now() };

  return jwt.sign(claims, env.JWT_PRIVATE_KEY, {
    algorithm: "HS512",
    jwtid: source,
  });
}
