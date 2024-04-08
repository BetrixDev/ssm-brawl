import { db } from "tussler";
import { internalProcedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";

export const langRouter = router({
  getAllEntries: internalProcedure.query(async () => {
    return queryClient.fetchQuery({
      queryKey: ["lang", "all"],
      queryFn: async () => {
        const langEntries = await db.query.lang.findMany();

        return langEntries.reduce((prev, curr) => {
          return { ...prev, [curr.id]: curr.text };
        }, {});
      },
    });
  }),
});
