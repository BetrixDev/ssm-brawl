export type CreateContextOptions = {
  request: Request;
};

export async function createContext({ request }: CreateContextOptions) {
  return {
    headers: request.headers,
  };
}

export type Context = Awaited<ReturnType<typeof createContext>>;
