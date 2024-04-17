CREATE TABLE `abilities` (
	`id` text PRIMARY KEY NOT NULL,
	`meta` text,
	`cooldown` integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE `abilities_to_kits` (
	`kit_id` text NOT NULL,
	`ability_id` text NOT NULL,
	`ability_tool_slot` integer NOT NULL,
	PRIMARY KEY(`ability_id`, `kit_id`)
);
--> statement-breakpoint
CREATE TABLE `basic_player_data` (
	`uuid` text(36) PRIMARY KEY NOT NULL,
	`selected_kit_id` text DEFAULT 'creeper' NOT NULL,
	`total_games_played` integer DEFAULT 0 NOT NULL,
	`total_games_won` integer DEFAULT 0 NOT NULL,
	`total_playtime_seconds` integer DEFAULT 0 NOT NULL,
	`is_banned` integer DEFAULT false NOT NULL,
	`level_experience` integer DEFAULT 0 NOT NULL,
	`rank_elo` integer DEFAULT 0 NOT NULL,
	`ranked_matches_played` integer DEFAULT 0 NOT NULL,
	`are_friend_requests_off` integer DEFAULT false NOT NULL,
	`can_receive_random_messages` integer DEFAULT true NOT NULL
);
--> statement-breakpoint
CREATE TABLE `disguises` (
	`id` text PRIMARY KEY NOT NULL,
	`display_entity` text NOT NULL,
	`hurt_sound` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `friendships` (
	`uuid_1` text NOT NULL,
	`uuid_2` text NOT NULL,
	PRIMARY KEY(`uuid_1`, `uuid_2`)
);
--> statement-breakpoint
CREATE TABLE `ip_bans` (
	`ip` text PRIMARY KEY NOT NULL,
	`is_banned` integer DEFAULT true NOT NULL
);
--> statement-breakpoint
CREATE TABLE `kits` (
	`id` text PRIMARY KEY NOT NULL,
	`melee_damage` real NOT NULL,
	`armor` real NOT NULL,
	`knockback_mult` real DEFAULT 1 NOT NULL,
	`inventory_icon` text NOT NULL,
	`disguise_id` text NOT NULL,
	`helmet_id` text,
	`chestplate_id` text,
	`leggings_id` text,
	`boots_id` text,
	`hitbox_width` real DEFAULT 0.6 NOT NULL,
	`hitbox_height` real DEFAULT 1.8 NOT NULL,
	`meta` text
);
--> statement-breakpoint
CREATE TABLE `lang` (
	`id` text PRIMARY KEY NOT NULL,
	`text` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `map_origins` (
	`map_id` text PRIMARY KEY NOT NULL,
	`x` real NOT NULL,
	`y` real NOT NULL,
	`z` real NOT NULL
);
--> statement-breakpoint
CREATE TABLE `map_spawnpoints` (
	`map_id` text NOT NULL,
	`x` real NOT NULL,
	`y` real NOT NULL,
	`z` real NOT NULL,
	PRIMARY KEY(`map_id`, `x`, `y`, `z`)
);
--> statement-breakpoint
CREATE TABLE `maps` (
	`id` text PRIMARY KEY NOT NULL,
	`min_players` integer NOT NULL,
	`max_players` integer NOT NULL,
	`origin_id` text NOT NULL,
	`world_border_radius` integer NOT NULL,
	`role` text DEFAULT 'game' NOT NULL
);
--> statement-breakpoint
CREATE TABLE `minigames` (
	`id` text PRIMARY KEY NOT NULL,
	`min_players` integer NOT NULL,
	`max_players` integer NOT NULL,
	`players_per_team` integer DEFAULT 1 NOT NULL,
	`countdown_seconds` integer DEFAULT 5 NOT NULL,
	`stocks` integer DEFAULT 4 NOT NULL
);
--> statement-breakpoint
CREATE TABLE `passives` (
	`id` text PRIMARY KEY NOT NULL,
	`meta` text
);
--> statement-breakpoint
CREATE TABLE `passives_to_kits` (
	`kit_id` text NOT NULL,
	`passive_id` text NOT NULL,
	`meta` text,
	PRIMARY KEY(`kit_id`, `passive_id`)
);
--> statement-breakpoint
CREATE TABLE `queue` (
	`player_uuid` text(36) PRIMARY KEY NOT NULL,
	`minigame_id` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `abilities_id_idx` ON `abilities` (`id`);--> statement-breakpoint
CREATE INDEX `atk_kit_id_idx` ON `abilities_to_kits` (`kit_id`);--> statement-breakpoint
CREATE INDEX `atk_ability_id_idx` ON `abilities_to_kits` (`ability_id`);--> statement-breakpoint
CREATE INDEX `b_player_uuid_idx` ON `basic_player_data` (`uuid`);--> statement-breakpoint
CREATE INDEX `friendships_uuid_1_idx` ON `friendships` (`uuid_1`);--> statement-breakpoint
CREATE INDEX `friendships_uuid_2_idx` ON `friendships` (`uuid_2`);--> statement-breakpoint
CREATE INDEX `ip_idx` ON `ip_bans` (`ip`);--> statement-breakpoint
CREATE INDEX `kits_id_idx` ON `kits` (`id`);--> statement-breakpoint
CREATE INDEX `spawnpoints_map_id_idx` ON `map_spawnpoints` (`map_id`);--> statement-breakpoint
CREATE INDEX `maps_id_idx` ON `maps` (`id`);--> statement-breakpoint
CREATE INDEX `minigames_id_idx` ON `minigames` (`id`);--> statement-breakpoint
CREATE INDEX `passives_id_idx` ON `passives` (`id`);--> statement-breakpoint
CREATE INDEX `ptk_kit_id_idx` ON `passives_to_kits` (`kit_id`);--> statement-breakpoint
CREATE INDEX `ptk_passive_id_idx` ON `passives_to_kits` (`passive_id`);--> statement-breakpoint
CREATE INDEX `queue_minigame_id_idx` ON `queue` (`minigame_id`);--> statement-breakpoint
CREATE INDEX `queue_player_uuid_idx` ON `queue` (`player_uuid`);