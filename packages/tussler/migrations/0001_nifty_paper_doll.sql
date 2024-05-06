CREATE TABLE `parties` (
	`party_id` text PRIMARY KEY NOT NULL,
	`owner_uuid` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `party_guests` (
	`player_uuid` text PRIMARY KEY NOT NULL,
	`party_id` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `party_invites` (
	`party_id` text NOT NULL,
	`inviter_uuid` text NOT NULL,
	`invitee_uuid` text NOT NULL,
	PRIMARY KEY(`invitee_uuid`, `inviter_uuid`, `party_id`)
);
--> statement-breakpoint
CREATE TABLE `usercache` (
	`uuid` text(36) PRIMARY KEY NOT NULL,
	`username` text NOT NULL
);
--> statement-breakpoint
ALTER TABLE minigames ADD `amount_of_teams` integer DEFAULT 4 NOT NULL;--> statement-breakpoint
ALTER TABLE minigames ADD `is_hidden` integer DEFAULT false NOT NULL;--> statement-breakpoint
ALTER TABLE queue ADD `party_id` text;--> statement-breakpoint
ALTER TABLE queue ADD `date_added` integer NOT NULL;--> statement-breakpoint
CREATE INDEX `p_guest_party_id_idx` ON `party_guests` (`party_id`);--> statement-breakpoint
CREATE INDEX `p_guest_player_uuid_idx` ON `party_guests` (`player_uuid`);--> statement-breakpoint
CREATE INDEX `usercache_player_uuid_idx` ON `usercache` (`uuid`);