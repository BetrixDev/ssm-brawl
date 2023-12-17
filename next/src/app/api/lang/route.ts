import { db, langTable } from "@/db";
import { isAuthedForRequest } from "@/utils";

type GroupedLang = Record<string, Record<string, string>>;

export async function POST(request: Request) {
  if (!isAuthedForRequest(request)) {
    return new Response(undefined, {
      status: 403,
    });
  }

  const langEntries = await db.select().from(langTable).execute();

  const groupedLang = langEntries.reduce<GroupedLang>(
    (acc, curr) => ({
      ...acc,
      [curr.locale]: { ...acc[curr.locale], [curr.key]: curr.string },
    }),
    {}
  );

  return new Response(JSON.stringify(groupedLang));
}
