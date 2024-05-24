import { db } from "tussler";
import { internalProcedure, router } from "../trpc.js";

export const langRouter = router({
  getAllEntries: internalProcedure.query(async () => {
    const langEntries = await db.query.lang.findMany();

    return langEntries.reduce((prev, curr) => {
      return { ...prev, [curr.id]: curr.text };
    }, {});
  }),
});
