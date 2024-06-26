CREATE TABLE IF NOT EXISTS "abilities" (
	"id" varchar PRIMARY KEY NOT NULL,
	"meta" jsonb,
	"cooldown" integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "abilities_to_kits" (
	"kit_id" varchar NOT NULL,
	"ability_id" varchar NOT NULL,
	"ability_tool_slot" smallint NOT NULL,
	CONSTRAINT "abilities_to_kits_pk" PRIMARY KEY("ability_id","kit_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "basic_player_data" (
	"uuid" varchar(36) PRIMARY KEY NOT NULL,
	"selected_kit_id" varchar DEFAULT 'creeper' NOT NULL,
	"total_games_played" bigint DEFAULT 0 NOT NULL,
	"total_games_won" bigint DEFAULT 0 NOT NULL,
	"total_playtime_seconds" bigint DEFAULT 0 NOT NULL,
	"is_banned" boolean DEFAULT false NOT NULL,
	"level_experience" bigint DEFAULT 0 NOT NULL,
	"rank_elo" bigint DEFAULT 0 NOT NULL,
	"ranked_matches_played" bigint DEFAULT 0 NOT NULL,
	"are_friend_requests_off" boolean DEFAULT false NOT NULL,
	"can_receive_random_messages" boolean DEFAULT true NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "disguises" (
	"id" varchar PRIMARY KEY NOT NULL,
	"display_entity" varchar NOT NULL,
	"hurt_sound" varchar NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "friendships" (
	"uuid_1" varchar NOT NULL,
	"uuid_2" varchar NOT NULL,
	CONSTRAINT "friendships_uuid_1_uuid_2_pk" PRIMARY KEY("uuid_1","uuid_2")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "ip_bans" (
	"ip" varchar PRIMARY KEY NOT NULL,
	"is_banned" boolean DEFAULT true NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "kits" (
	"id" varchar PRIMARY KEY NOT NULL,
	"melee_damage" real NOT NULL,
	"armor" real NOT NULL,
	"knockback_mult" real DEFAULT 1 NOT NULL,
	"inventory_icon" varchar NOT NULL,
	"disguise_id" varchar NOT NULL,
	"helmet_id" varchar,
	"chestplate_id" varchar,
	"leggings_id" varchar,
	"boots_id" varchar,
	"hitbox_width" real DEFAULT 0.6 NOT NULL,
	"hitbox_height" real DEFAULT 1.8 NOT NULL,
	"meta" jsonb
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "lang" (
	"id" varchar PRIMARY KEY NOT NULL,
	"varchar" varchar NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "maps" (
	"id" varchar PRIMARY KEY NOT NULL,
	"min_players" integer NOT NULL,
	"max_players" integer NOT NULL,
	"origin" jsonb NOT NULL,
	"spawn_points" jsonb NOT NULL,
	"world_border_radius" bigint NOT NULL,
	"role" varchar DEFAULT 'game' NOT NULL,
	"void_y_level" real DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "message_channels" (
	"id" varchar PRIMARY KEY NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "message_viewers" (
	"channel_id" varchar NOT NULL,
	"player_uuid" varchar NOT NULL,
	CONSTRAINT "message_viewer_pk" PRIMARY KEY("channel_id","player_uuid")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "messages" (
	"id" varchar PRIMARY KEY NOT NULL,
	"channel_id" varchar NOT NULL,
	"content" varchar NOT NULL,
	"author_uuid" varchar NOT NULL,
	"time" bigint NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "minigames" (
	"id" varchar PRIMARY KEY NOT NULL,
	"min_players" integer NOT NULL,
	"max_players" integer NOT NULL,
	"players_per_team" integer DEFAULT 1 NOT NULL,
	"amount_of_teams" integer DEFAULT 4 NOT NULL,
	"countdown_seconds" integer DEFAULT 5 NOT NULL,
	"is_hidden" boolean DEFAULT false NOT NULL,
	"stocks" integer DEFAULT 4 NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "parties" (
	"party_id" varchar PRIMARY KEY NOT NULL,
	"owner_uuid" varchar NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "party_guests" (
	"player_uuid" varchar PRIMARY KEY NOT NULL,
	"party_id" varchar NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "party_invites" (
	"party_id" varchar NOT NULL,
	"inviter_uuid" varchar NOT NULL,
	"invitee_uuid" varchar NOT NULL,
	CONSTRAINT "party_invite_pk" PRIMARY KEY("invitee_uuid","inviter_uuid","party_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "passives" (
	"id" varchar PRIMARY KEY NOT NULL,
	"meta" jsonb
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "passives_to_kits" (
	"kit_id" varchar NOT NULL,
	"passive_id" varchar NOT NULL,
	"meta" jsonb,
	CONSTRAINT "passives_to_kits_pk" PRIMARY KEY("passive_id","kit_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "queue" (
	"player_uuid" varchar(36) PRIMARY KEY NOT NULL,
	"party_id" varchar,
	"date_added" bigint NOT NULL,
	"minigame_id" varchar NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "usercache" (
	"uuid" varchar(36) PRIMARY KEY NOT NULL,
	"username" varchar NOT NULL
);
--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "abilities_id_idx" ON "abilities" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "atk_kit_id_idx" ON "abilities_to_kits" ("kit_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "atk_ability_id_idx" ON "abilities_to_kits" ("ability_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "b_player_uuid_idx" ON "basic_player_data" ("uuid");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "friendships_uuid_1_idx" ON "friendships" ("uuid_1");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "friendships_uuid_2_idx" ON "friendships" ("uuid_2");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "ip_idx" ON "ip_bans" ("ip");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "kits_id_idx" ON "kits" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "maps_id_idx" ON "maps" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "mc_id_idx" ON "message_channels" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "mv_channel_id_idx" ON "message_viewers" ("channel_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "mv_player_uuid_idx" ON "message_viewers" ("player_uuid");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "m_message_id_idx" ON "messages" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "m_author_uuid_idx" ON "messages" ("author_uuid");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "minigames_id_idx" ON "minigames" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "p_guest_party_id_idx" ON "party_guests" ("party_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "p_guest_player_uuid_idx" ON "party_guests" ("player_uuid");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "passives_id_idx" ON "passives" ("id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "ptk_kit_id_idx" ON "passives_to_kits" ("kit_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "ptk_passive_id_idx" ON "passives_to_kits" ("passive_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "queue_minigame_id_idx" ON "queue" ("minigame_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "queue_player_uuid_idx" ON "queue" ("player_uuid");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "usercache_player_uuid_idx" ON "usercache" ("uuid");