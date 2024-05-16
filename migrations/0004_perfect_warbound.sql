CREATE TABLE `message_channels` (
	`id` text PRIMARY KEY NOT NULL
);
--> statement-breakpoint
CREATE TABLE `message_viewers` (
	`channel_id` text NOT NULL,
	`player_uuid` text NOT NULL,
	PRIMARY KEY(`channel_id`, `player_uuid`)
);
--> statement-breakpoint
CREATE TABLE `messages` (
	`id` text PRIMARY KEY NOT NULL,
	`channel_id` text NOT NULL,
	`content` text NOT NULL,
	`author_uuid` text NOT NULL,
	`time` integer NOT NULL
);
--> statement-breakpoint
CREATE INDEX `mc_id_idx` ON `message_channels` (`id`);--> statement-breakpoint
CREATE INDEX `mv_channel_id_idx` ON `message_viewers` (`channel_id`);--> statement-breakpoint
CREATE INDEX `mv_player_uuid_idx` ON `message_viewers` (`player_uuid`);--> statement-breakpoint
CREATE INDEX `m_message_id_idx` ON `messages` (`id`);--> statement-breakpoint
CREATE INDEX `m_author_uuid_idx` ON `messages` (`author_uuid`);