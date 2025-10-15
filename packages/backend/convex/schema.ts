import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  players: defineTable({
    uuid: v.string(),
    username: v.string(),
    firstJoinedDate: v.string(),
    lastJoinedDate: v.string(),
    headSkinBase64: v.string(),
    selectedKitId: v.string(),
  }).searchIndex("search_username", {
    searchField: "username",
    filterFields: ["uuid"],
  }),
});
