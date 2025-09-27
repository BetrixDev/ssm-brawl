import { kvProvider, pluginProcedure } from "@/lib/orpc";
import z from "zod";

export const pluginKvRouter = {
    get: pluginProcedure
    .route({ method: "GET", path: "/{key}" })
    .output(z.any().nullable())
    .input(z.object({ key: z.string() }))
    .use(kvProvider)
    .handler(async ({context, input}) => {
        return await context.kv.get(input.key);
    }),
    set: pluginProcedure
    .route({ method: "POST", path: "/{key}" })
    .output(z.object({ message: z.string() }))
    .input(z.object({ key: z.string(), value: z.any() }))
    .use(kvProvider)
    .handler(async ({context, input}) => {
        await context.kv.set(input.key, input.value);

        return { message: "KV set" };
    }),
    delete: pluginProcedure
    .route({ method: "DELETE", path: "/{key}" })
    .output(z.object({ message: z.string() }))
    .input(z.object({ key: z.string() }))
    .use(kvProvider)
    .handler(async ({context, input}) => {
        await context.kv.delete(input.key);
        
        return { message: "KV deleted" };
    }),
}