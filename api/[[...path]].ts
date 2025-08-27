import app from "../backend/dist/index.js";

function getOriginalRequestUrl(req: any): string {
  const proto = (req.headers["x-forwarded-proto"] as string) || "https";
  const host =
    (req.headers["x-forwarded-host"] as string) || req.headers.host || "localhost";
  const currentUrl = new URL(req.url || "/", `${proto}://${host}`);

  // Reconstruct the original path (strip the /api prefix added by Vercel routing)
  let pathname = currentUrl.pathname || "/";
  if (pathname === "/api") pathname = "/";
  else if (pathname.startsWith("/api/")) pathname = pathname.slice(4) || "/";

  const originalUrl = new URL(pathname + currentUrl.search, `${proto}://${host}`);
  return originalUrl.toString();
}

function nodeHeadersToWebHeaders(nodeHeaders: Record<string, string | string[] | undefined>): Headers {
  const headers = new Headers();
  for (const [key, value] of Object.entries(nodeHeaders)) {
    if (typeof value === "undefined") continue;
    if (Array.isArray(value)) {
      for (const v of value) headers.append(key, v);
    } else {
      headers.append(key, String(value));
    }
  }
  return headers;
}

async function buildWebRequestFromNode(req: any): Promise<Request> {
  const method = req.method || "GET";
  const headers = nodeHeadersToWebHeaders(req.headers || {});
  if (method === "GET" || method === "HEAD") {
    return new Request(getOriginalRequestUrl(req), { method, headers });
  }
  const chunks: Buffer[] = [];
  for await (const chunk of req) {
    chunks.push(typeof chunk === "string" ? Buffer.from(chunk) : chunk);
  }
  const body = Buffer.concat(chunks);
  return new Request(getOriginalRequestUrl(req), { method, headers, body });
}

export default async function handler(req: any, res: any) {
  try {
    const request = await buildWebRequestFromNode(req);
    const response = await app.handle(request);

    res.statusCode = response.status;
    response.headers.forEach((value, key) => {
      res.setHeader(key, value);
    });

    if (!response.body) {
      res.end();
      return;
    }

    const buffer = Buffer.from(await response.arrayBuffer());
    res.end(buffer);
  } catch (error) {
    console.error("Error in API handler:", error);
    res.statusCode = 500;
    res.end("Internal Server Error");
  }
}

