CREATE TABLE "player_join_events" (
	"id" text PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL,
	"ip_address" text
);
--> statement-breakpoint
ALTER TABLE "player_bans" DROP CONSTRAINT "player_bans_player_uuid_players_uuid_fk";
--> statement-breakpoint
ALTER TABLE "player_bans" ALTER COLUMN "player_uuid" SET NOT NULL;--> statement-breakpoint
ALTER TABLE "players" ALTER COLUMN "stats" SET DEFAULT '{}'::jsonb;--> statement-breakpoint
ALTER TABLE "player_join_events" ADD CONSTRAINT "player_join_events_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_bans" ADD CONSTRAINT "player_bans_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;