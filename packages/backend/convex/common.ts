import { NoOp } from "convex-helpers/server/customFunctions";
import { zCustomMutation, zCustomQuery } from "convex-helpers/server/zod";
import { internalMutation, internalQuery, mutation, query } from "./_generated/server";

export const zQuery = zCustomQuery(query, NoOp);

export const zMutation = zCustomMutation(mutation, NoOp);

export const zInternalQuery = zCustomQuery(internalQuery, NoOp);

export const zInternalMutation = zCustomMutation(internalMutation, NoOp);
