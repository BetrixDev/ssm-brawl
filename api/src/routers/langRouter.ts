import { db } from "../db/db.js";
import { procedure, router } from "../trpc.js";
import { queryClient } from "../utils/query-client.js";

export const langRouter = router({
  getAllEntries: procedure.query(async () => {
    return queryClient.fetchQuery({
      queryKey: ["lang", "all"],
      queryFn: async () => {
        const langEntries = await db.query.langTable.findMany();

        return langEntries.reduce((prev, curr) => {
          return { ...prev, [curr.id]: curr.text };
        }, {});
      },
    });
  }),
});
