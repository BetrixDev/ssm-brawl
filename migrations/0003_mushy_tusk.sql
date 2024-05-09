DROP TABLE `map_origins`;--> statement-breakpoint
DROP TABLE `map_spawnpoints`;--> statement-breakpoint
ALTER TABLE maps ADD `origin` text NOT NULL;--> statement-breakpoint
ALTER TABLE maps ADD `spawn_points` text NOT NULL;--> statement-breakpoint
ALTER TABLE `maps` DROP COLUMN `origin_id`;