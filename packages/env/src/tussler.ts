import z from "zod";

const envSchema = z.discriminatedUnion("TUSSLER_TYPE", [
  z.object({
    TUSSLER_TYPE: z.literal("pglite"),
    NODE_ENV: z.string().optional(),
  }),
  z.object({
    TUSSLER_TYPE: z.literal("postgres"),
    TUSSLER_HOST: z.string(),
    TUSSLER_PASSWORD: z.string(),
    NODE_ENV: z.string().optional(),
  }),
]);

const env = envSchema.parse(process.env);

export { env };
