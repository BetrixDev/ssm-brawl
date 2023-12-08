import { db, queueTable } from "@/db";
import { isAuthedForRequest } from "@/utils";

export async function DELETE(request: Request) {
  if (!isAuthedForRequest(request)) {
    return new Response(undefined, {
      status: 403,
    });
  }

  console.log("clearing");

  await db.delete(queueTable).execute();

  return new Response(undefined, { status: 200 });
}
