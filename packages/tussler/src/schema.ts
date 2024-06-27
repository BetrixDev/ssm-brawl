import { relations } from "drizzle-orm";
import {
  real,
  pgTable,
  primaryKey,
  varchar,
  jsonb,
  index,
  bigint,
  integer,
  smallint,
  boolean,
} from "drizzle-orm/pg-core";

export type MapRole = "game" | "hub";

export type Vector3 = { x: number; y: number; z: number };

export const lang = pgTable("lang", {
  id: varchar("id").notNull().primaryKey(),
  text: varchar("varchar").notNull(),
});

export const kits = pgTable(
  "kits",
  {
    id: varchar("id").primaryKey().notNull(),
    meleeDamage: real("melee_damage").notNull(),
    armor: real("armor").notNull(),
    knockbackMult: real("knockback_mult").default(1.0).notNull(),
    inventoryIcon: varchar("inventory_icon").notNull(),
    disguiseId: varchar("disguise_id").notNull(),
    helmetId: varchar("helmet_id"),
    chestplateId: varchar("chestplate_id"),
    leggingsId: varchar("leggings_id"),
    bootsId: varchar("boots_id"),
    hitboxWidth: real("hitbox_width").notNull().default(0.6),
    hitboxHeight: real("hitbox_height").notNull().default(1.8),
    meta: jsonb("meta").$type<Record<string, string>>(),
  },
  (table) => ({
    idIdx: index("kits_id_idx").on(table.id),
  }),
);

export const abilities = pgTable(
  "abilities",
  {
    id: varchar("id").primaryKey().notNull(),
    meta: jsonb("meta"),
    cooldown: integer("cooldown").notNull(),
  },
  (table) => ({
    idIdx: index("abilities_id_idx").on(table.id),
  }),
);

export const disguises = pgTable("disguises", {
  id: varchar("id").primaryKey().notNull(),
  displayEntity: varchar("display_entity").notNull(),
  hurtSound: varchar("hurt_sound").notNull(),
});

export const abilitiesToKits = pgTable(
  "abilities_to_kits",
  {
    kitId: varchar("kit_id")
      .notNull()
      .references(() => kits.id),
    abilityId: varchar("ability_id")
      .notNull()
      .references(() => abilities.id),
    abilityToolSlot: smallint("ability_tool_slot").notNull(),
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

export const passives = pgTable(
  "passives",
  {
    id: varchar("id").primaryKey().notNull(),
    meta: jsonb("meta").$type<Record<string, string>>(),
  },
  (table) => ({
    idIdx: index("passives_id_idx").on(table.id),
  }),
);

export const passivesToKits = pgTable(
  "passives_to_kits",
  {
    kitId: varchar("kit_id")
      .notNull()
      .references(() => kits.id),
    passiveId: varchar("passive_id")
      .notNull()
      .references(() => passives.id),
    meta: jsonb("meta").$type<Record<string, string>>(),
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

export const basicPlayerData = pgTable(
  "basic_player_data",
  {
    uuid: varchar("uuid", { length: 36 }).primaryKey(),
    selectedKitId: varchar("selected_kit_id").default("creeper").notNull(),
    totalGamesPlayed: bigint("total_games_played", { mode: "number" }).default(0).notNull(),
    totalGamesWon: bigint("total_games_won", { mode: "number" }).default(0).notNull(),
    totalPlaytimeSeconds: bigint("total_playtime_seconds", { mode: "number" }).default(0).notNull(),
    isBanned: boolean("is_banned").default(false).notNull(),
    levelExperience: bigint("level_experience", { mode: "number" }).default(0).notNull(),
    rankElo: bigint("rank_elo", { mode: "number" }).default(0).notNull(),
    rankedMatchesPlayed: bigint("ranked_matches_played", { mode: "number" }).default(0).notNull(),
    areFriendRequestsOff: boolean("are_friend_requests_off").default(false).notNull(),
    canReceiveRandomMessages: boolean("can_receive_random_messages").default(true).notNull(),
  },
  (table) => ({
    uuidIdx: index("b_player_uuid_idx").on(table.uuid),
  }),
);

export const usercache = pgTable(
  "usercache",
  {
    uuid: varchar("uuid", { length: 36 }).primaryKey(),
    username: varchar("username").notNull(),
  },
  (table) => ({
    uuidIdx: index("usercache_player_uuid_idx").on(table.uuid),
  }),
);

export const ipBans = pgTable(
  "ip_bans",
  {
    ip: varchar("ip").primaryKey().notNull(),
    isBanned: boolean("is_banned").notNull().default(true),
  },
  (table) => ({
    ipIdx: index("ip_idx").on(table.ip),
  }),
);

export const minigames = pgTable(
  "minigames",
  {
    id: varchar("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    playersPerTeam: integer("players_per_team").notNull().default(1),
    amountOfTeams: integer("amount_of_teams").notNull().default(4),
    countdownSeconds: integer("countdown_seconds").notNull().default(5),
    isHidden: boolean("is_hidden").notNull().default(false),
    stocks: integer("stocks").notNull().default(4),
  },
  (table) => ({
    idIdx: index("minigames_id_idx").on(table.id),
  }),
);

export const queue = pgTable(
  "queue",
  {
    playerUuid: varchar("player_uuid", { length: 36 })
      .primaryKey()
      .references(() => basicPlayerData.uuid),
    partyId: varchar("party_id").references(() => parties.partyId),
    dateAdded: bigint("date_added", { mode: "number" }).notNull(),
    minigameId: varchar("minigame_id")
      .notNull()
      .references(() => minigames.id),
  },
  (table) => ({
    minigameIdIdx: index("queue_minigame_id_idx").on(table.minigameId),
    playerUuidIdx: index("queue_player_uuid_idx").on(table.playerUuid),
  }),
);

export const maps = pgTable(
  "maps",
  {
    id: varchar("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    origin: jsonb("origin").$type<Vector3>().notNull(),
    spawnPoints: jsonb("spawn_points").$type<Vector3[]>().notNull(),
    worldBorderRadius: bigint("world_border_radius", { mode: "number" }).notNull(),
    role: varchar("role", { enum: ["game", "hub"] })
      .$type<MapRole>()
      .notNull()
      .default("game"),
    voidYLevel: real("void_y_level").default(0).notNull(),
  },
  (table) => ({
    idIdx: index("maps_id_idx").on(table.id),
  }),
);

export const friendships = pgTable(
  "friendships",
  {
    uuid1: varchar("uuid_1")
      .notNull()
      .references(() => basicPlayerData.uuid),
    uuid2: varchar("uuid_2")
      .notNull()
      .references(() => basicPlayerData.uuid),
  },
  (table) => ({
    friendshipsPk: primaryKey({ columns: [table.uuid1, table.uuid2] }),
    friendshipsUuid1Idx: index("friendships_uuid_1_idx").on(table.uuid1),
    friendshipsUuid2Idx: index("friendships_uuid_2_idx").on(table.uuid2),
  }),
);

export const parties = pgTable("parties", {
  partyId: varchar("party_id").primaryKey(),
  ownerUuid: varchar("owner_uuid")
    .notNull()
    .references(() => basicPlayerData.uuid, { onDelete: "cascade" }),
});

export const partyGuests = pgTable(
  "party_guests",
  {
    playerUuid: varchar("player_uuid")
      .primaryKey()
      .references(() => basicPlayerData.uuid),
    partyId: varchar("party_id")
      .notNull()
      .references(() => parties.partyId),
  },
  (table) => ({
    pGuestPartyIdIdx: index("p_guest_party_id_idx").on(table.partyId),
    pGuestPlayerUuidIdx: index("p_guest_player_uuid_idx").on(table.playerUuid),
  }),
);

export const partyInvites = pgTable(
  "party_invites",
  {
    partyId: varchar("party_id")
      .notNull()
      .references(() => parties.partyId),
    inviterUuid: varchar("inviter_uuid")
      .notNull()
      .references(() => basicPlayerData.uuid),
    inviteeUuid: varchar("invitee_uuid")
      .notNull()
      .references(() => basicPlayerData.uuid),
  },
  (table) => ({
    partyInvitePk: primaryKey({
      name: "party_invite_pk",
      columns: [table.inviteeUuid, table.inviterUuid, table.partyId],
    }),
  }),
);

export const messages = pgTable(
  "messages",
  {
    id: varchar("id").primaryKey(),
    channelId: varchar("channel_id")
      .notNull()
      .references(() => messageChannels.id),
    content: varchar("content").notNull(),
    authorUuid: varchar("author_uuid")
      .notNull()
      .references(() => basicPlayerData.uuid),
    time: bigint("time", { mode: "number" }).notNull(),
  },
  (table) => ({
    mMessageIdIdx: index("m_message_id_idx").on(table.id),
    mAuthorUuidIdx: index("m_author_uuid_idx").on(table.authorUuid),
  }),
);

export const messageChannels = pgTable(
  "message_channels",
  {
    id: varchar("id").primaryKey(),
  },
  (table) => ({
    mcIdIdx: index("mc_id_idx").on(table.id),
  }),
);

export const messageViewers = pgTable(
  "message_viewers",
  {
    channelId: varchar("channel_id")
      .notNull()
      .references(() => messageChannels.id),
    playerUuid: varchar("player_uuid")
      .notNull()
      .references(() => basicPlayerData.uuid),
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
