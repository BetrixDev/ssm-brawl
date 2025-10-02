import { deleteKv, getKv, setKv } from "@/db/queries/kv";
import { pluginProcedure } from "@/lib/orpc";
import z from "zod";

export const pluginKvRouter = {
  get: pluginProcedure
    .route({ method: "GET", path: "/{key}" })
    .output(z.any().nullable())
    .input(z.object({ key: z.string() }))
    .handler(async ({ context, input }) => {
      return await getKv(input.key);
    }),
  set: pluginProcedure
    .route({ method: "POST", path: "/{key}" })
    .output(z.object({ message: z.string() }))
    .input(z.object({ key: z.string(), value: z.unknown() }))
    .handler(async ({ input }) => {
      await setKv(input.key, input.value);

      return { message: "KV set" };
    }),
  delete: pluginProcedure
    .route({ method: "DELETE", path: "/{key}" })
    .output(z.object({ message: z.string() }))
    .input(z.object({ key: z.string() }))
    .handler(async ({ input }) => {
      await deleteKv(input.key);

      return { message: "KV deleted" };
    }),
};
