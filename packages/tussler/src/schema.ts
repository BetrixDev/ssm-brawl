import { relations } from "drizzle-orm";
import { real, index, sqliteTable, primaryKey, integer, text, int } from "drizzle-orm/sqlite-core";

export type MapRole = "game" | "hub";

export type Vector3 = { x: number; y: number; z: number };

export const lang = sqliteTable("lang", {
  id: text("id").notNull().primaryKey(),
  text: text("text").notNull(),
});

export const kits = sqliteTable(
  "kits",
  {
    id: text("id").primaryKey().notNull(),
    meleeDamage: real("melee_damage").notNull(),
    armor: real("armor").notNull(),
    knockbackMult: real("knockback_mult").default(1.0).notNull(),
    inventoryIcon: text("inventory_icon").notNull(),
    disguiseId: text("disguise_id").notNull(),
    helmetId: text("helmet_id"),
    chestplateId: text("chestplate_id"),
    leggingsId: text("leggings_id"),
    bootsId: text("boots_id"),
    hitboxWidth: real("hitbox_width").notNull().default(0.6),
    hitboxHeight: real("hitbox_height").notNull().default(1.8),
    meta: text("meta", { mode: "json" }).$type<Record<string, string>>(),
  },
  (table) => ({
    idIdx: index("kits_id_idx").on(table.id),
  }),
);

export const abilities = sqliteTable(
  "abilities",
  {
    id: text("id").primaryKey().notNull(),
    meta: text("meta", { mode: "json" }),
    cooldown: int("cooldown").notNull(),
  },
  (table) => ({
    idIdx: index("abilities_id_idx").on(table.id),
  }),
);

export const disguises = sqliteTable("disguises", {
  id: text("id").primaryKey().notNull(),
  displayEntity: text("display_entity").notNull(),
  hurtSound: text("hurt_sound").notNull(),
});

export const abilitiesToKits = sqliteTable(
  "abilities_to_kits",
  {
    kitId: text("kit_id").notNull(),
    abilityId: text("ability_id").notNull(),
    abilityToolSlot: int("ability_tool_slot").notNull(),
  },
  (table) => ({
    kitIdIdx: index("atk_kit_id_idx").on(table.kitId),
    abilityIdIdx: index("atk_ability_id_idx").on(table.abilityId),
    pk: primaryKey({
      name: "abilities_to_kits_pk",
      columns: [table.abilityId, table.kitId],
    }),
  }),
);

export const passives = sqliteTable(
  "passives",
  {
    id: text("id").primaryKey().notNull(),
    meta: text("meta", { mode: "json" }).$type<Record<string, string>>(),
  },
  (table) => ({
    idIdx: index("passives_id_idx").on(table.id),
  }),
);

export const passivesToKits = sqliteTable(
  "passives_to_kits",
  {
    kitId: text("kit_id").notNull(),
    passiveId: text("passive_id").notNull(),
    meta: text("meta", { mode: "json" }).$type<Record<string, string>>(),
  },
  (table) => ({
    kitIdIdx: index("ptk_kit_id_idx").on(table.kitId),
    passiveIdIdx: index("ptk_passive_id_idx").on(table.passiveId),
    pk: primaryKey({
      name: "passives_to_kits_pk",
      columns: [table.passiveId, table.kitId],
    }),
  }),
);

export const basicPlayerData = sqliteTable(
  "basic_player_data",
  {
    uuid: text("uuid", { length: 36 }).primaryKey(),
    selectedKitId: text("selected_kit_id").default("creeper").notNull(),
    totalGamesPlayed: int("total_games_played").default(0).notNull(),
    totalGamesWon: int("total_games_won").default(0).notNull(),
    totalPlaytimeSeconds: int("total_playtime_seconds").default(0).notNull(),
    isBanned: int("is_banned", { mode: "boolean" }).default(false).notNull(),
    levelExperience: int("level_experience").default(0).notNull(),
    rankElo: int("rank_elo").default(0).notNull(),
    rankedMatchesPlayed: int("ranked_matches_played").default(0).notNull(),
    areFriendRequestsOff: int("are_friend_requests_off", {
      mode: "boolean",
    })
      .default(false)
      .notNull(),
    canReceiveRandomMessages: int("can_receive_random_messages", {
      mode: "boolean",
    })
      .default(true)
      .notNull(),
  },
  (table) => ({
    uuidIdx: index("b_player_uuid_idx").on(table.uuid),
  }),
);

export const usercache = sqliteTable(
  "usercache",
  {
    uuid: text("uuid", { length: 36 }).primaryKey(),
    username: text("username").notNull(),
  },
  (table) => ({
    uuidIdx: index("usercache_player_uuid_idx").on(table.uuid),
  }),
);

export const ipBans = sqliteTable(
  "ip_bans",
  {
    ip: text("ip").primaryKey().notNull(),
    isBanned: int("is_banned", { mode: "boolean" }).notNull().default(true),
  },
  (table) => ({
    ipIdx: index("ip_idx").on(table.ip),
  }),
);

export const minigames = sqliteTable(
  "minigames",
  {
    id: text("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    playersPerTeam: integer("players_per_team").notNull().default(1),
    amountOfTeams: integer("amount_of_teams").notNull().default(4),
    countdownSeconds: integer("countdown_seconds").notNull().default(5),
    isHidden: integer("is_hidden", { mode: "boolean" }).notNull().default(false),
    stocks: integer("stocks").notNull().default(4),
  },
  (table) => ({
    idIdx: index("minigames_id_idx").on(table.id),
  }),
);

export const queue = sqliteTable(
  "queue",
  {
    playerUuid: text("player_uuid", { length: 36 }).primaryKey(),
    partyId: text("party_id"),
    dateAdded: integer("date_added").notNull(),
    minigameId: text("minigame_id").notNull(),
  },
  (table) => ({
    minigameIdIdx: index("queue_minigame_id_idx").on(table.minigameId),
    playerUuidIdx: index("queue_player_uuid_idx").on(table.playerUuid),
  }),
);

export const maps = sqliteTable(
  "maps",
  {
    id: text("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    origin: text("origin", { mode: "json" }).$type<Vector3>().notNull(),
    spawnPoints: text("spawn_points", { mode: "json" }).$type<Vector3[]>().notNull(),
    worldBorderRadius: integer("world_border_radius").notNull(),
    role: text("role", { enum: ["game", "hub"] })
      .$type<MapRole>()
      .notNull()
      .default("game"),
    voidYLevel: real("void_y_level").default(0).notNull(),
  },
  (table) => ({
    idIdx: index("maps_id_idx").on(table.id),
  }),
);

export const friendships = sqliteTable(
  "friendships",
  {
    uuid1: text("uuid_1").notNull(),
    uuid2: text("uuid_2").notNull(),
  },
  (table) => ({
    friendshipsPk: primaryKey({ columns: [table.uuid1, table.uuid2] }),
    friendshipsUuid1Idx: index("friendships_uuid_1_idx").on(table.uuid1),
    friendshipsUuid2Idx: index("friendships_uuid_2_idx").on(table.uuid2),
  }),
);

export const parties = sqliteTable("parties", {
  partyId: text("party_id").primaryKey(),
  ownerUuid: text("owner_uuid").notNull(),
});

export const partyGuests = sqliteTable(
  "party_guests",
  {
    playerUuid: text("player_uuid").primaryKey(),
    partyId: text("party_id").notNull(),
  },
  (table) => ({
    pGuestPartyIdIdx: index("p_guest_party_id_idx").on(table.partyId),
    pGuestPlayerUuidIdx: index("p_guest_player_uuid_idx").on(table.playerUuid),
  }),
);

export const partyInvites = sqliteTable(
  "party_invites",
  {
    partyId: text("party_id").notNull(),
    inviterUuid: text("inviter_uuid").notNull(),
    inviteeUuid: text("invitee_uuid").notNull(),
  },
  (table) => ({
    partyInvitePk: primaryKey({
      name: "party_invite_pk",
      columns: [table.inviteeUuid, table.inviterUuid, table.partyId],
    }),
  }),
);

export const messages = sqliteTable(
  "messages",
  {
    id: text("id").primaryKey(),
    channelId: text("channel_id").notNull(),
    content: text("content").notNull(),
    authorUuid: text("author_uuid").notNull(),
    time: int("time").notNull(),
  },
  (table) => ({
    mMessageIdIdx: index("m_message_id_idx").on(table.id),
    mAuthorUuidIdx: index("m_author_uuid_idx").on(table.authorUuid),
  }),
);

export const messageChannels = sqliteTable(
  "message_channels",
  {
    id: text("id").primaryKey(),
  },
  (table) => ({
    mcIdIdx: index("mc_id_idx").on(table.id),
  }),
);

export const messageViewers = sqliteTable(
  "message_viewers",
  {
    channelId: text("channel_id").notNull(),
    playerUuid: text("player_uuid").notNull(),
  },
  (table) => ({
    messageViewerPk: primaryKey({
      name: "message_viewer_pk",
      columns: [table.channelId, table.playerUuid],
    }),
    mvChannelIdIdx: index("mv_channel_id_idx").on(table.channelId),
    mvPlayerUuidIdx: index("mv_player_uuid_idx").on(table.playerUuid),
  }),
);

export const messagesRelations = relations(messages, ({ one }) => ({
  channel: one(messageChannels, { fields: [messages.channelId], references: [messageChannels.id] }),
}));

export const messageChannelsRelations = relations(messageChannels, ({ many }) => ({
  messages: many(messages),
  messageViewers: many(messageViewers),
}));

export const messageViewersRelations = relations(messageViewers, ({ one }) => ({
  channel: one(messageChannels, {
    fields: [messageViewers.channelId],
    references: [messageChannels.id],
  }),
  viewer: one(basicPlayerData, {
    fields: [messageViewers.playerUuid],
    references: [basicPlayerData.uuid],
  }),
}));

export const partiesRelations = relations(parties, ({ many }) => ({
  guests: many(partyGuests),
}));

export const partyGuestsRelations = relations(partyGuests, ({ one }) => ({
  party: one(parties, {
    fields: [partyGuests.partyId],
    references: [parties.partyId],
  }),
}));

export const kitsRelations = relations(kits, ({ many, one }) => ({
  abilities: many(abilitiesToKits),
  passives: many(passivesToKits),
  disguise: one(disguises, {
    fields: [kits.disguiseId],
    references: [disguises.id],
  }),
}));

export const abilitiesRelations = relations(abilities, ({ many }) => ({
  kits: many(abilitiesToKits),
}));

export const passivesRelations = relations(passives, ({ many }) => ({
  kits: many(passivesToKits),
}));

export const abilitiesToKitsRelations = relations(abilitiesToKits, ({ one }) => ({
  kit: one(kits, {
    fields: [abilitiesToKits.kitId],
    references: [kits.id],
  }),
  ability: one(abilities, {
    fields: [abilitiesToKits.abilityId],
    references: [abilities.id],
  }),
}));

export const passivesToKitsRelations = relations(passivesToKits, ({ one }) => ({
  kit: one(kits, {
    fields: [passivesToKits.kitId],
    references: [kits.id],
  }),
  passive: one(passives, {
    fields: [passivesToKits.passiveId],
    references: [passives.id],
  }),
}));

export const minigamesRelations = relations(minigames, ({ many }) => ({
  queueEntries: many(queue),
}));

export const queueRelations = relations(queue, ({ one }) => ({
  minigame: one(minigames, {
    fields: [queue.minigameId],
    references: [minigames.id],
  }),
  player: one(basicPlayerData, {
    fields: [queue.playerUuid],
    references: [basicPlayerData.uuid],
  }),
}));

export const basicPlayerDataRelations = relations(basicPlayerData, ({ one }) => ({
  selectedKit: one(kits, {
    fields: [basicPlayerData.selectedKitId],
    references: [kits.id],
  }),
  usercache: one(usercache, {
    fields: [basicPlayerData.uuid],
    references: [usercache.uuid],
  }),
}));

export const usercacheRelations = relations(usercache, ({ one }) => ({
  basicPlayerData: one(basicPlayerData, {
    fields: [usercache.uuid],
    references: [basicPlayerData.uuid],
  }),
}));
