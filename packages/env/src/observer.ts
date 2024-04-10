import z from "zod";

const envSchema = z.object({
  AXIOM_DATASET: z.string(),
  AXIOM_TOKEN: z.string(),
  AXIOM_ORG_ID: z.string(),
});

const env = envSchema.parse(process.env);

export { env };
