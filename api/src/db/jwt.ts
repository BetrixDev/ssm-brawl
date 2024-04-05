import jwt from "jsonwebtoken";
import { env } from "env";
import typia from "typia";

export type BackendSource = "plugin" | "web" | "brawlie";

// Might also become valuable to include the uuid of the player
//  who is reponsible so triggering the backend request

type InternalClaims = {
  source: BackendSource;
};

type UserClaims = {
  uuid: string;
  source: "user";
};

export type JwtClaims = { iat: number; exp?: number } & (
  | InternalClaims
  | UserClaims
);

export async function decodeTokenFromHeaders(headers: Headers) {
  try {
    const cookieToken = headers.get("Cookie")?.split(" ")?.at(1);
    const authToken = headers.get("Authorization")?.split(" ")?.at(1);

    const encodedToken = cookieToken ?? authToken;

    if (!encodedToken) return;

    const decodedToken = typia.assert<JwtClaims>(
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
