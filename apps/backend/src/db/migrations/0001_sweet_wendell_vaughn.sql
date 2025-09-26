CREATE TABLE "player_bans" (
	"id" text PRIMARY KEY NOT NULL,
	"player_uuid" text,
	"reason" text NOT NULL,
	"expires_at" timestamp,
	"banned_at" timestamp DEFAULT now() NOT NULL,
	"banned_by" text NOT NULL
);
--> statement-breakpoint
ALTER TABLE "players" ADD COLUMN "first_joined_date" timestamp DEFAULT now() NOT NULL;--> statement-breakpoint
ALTER TABLE "players" ADD COLUMN "stats" jsonb NOT NULL;--> statement-breakpoint
ALTER TABLE "players" ADD COLUMN "head_skin_base64" text;--> statement-breakpoint
ALTER TABLE "player_bans" ADD CONSTRAINT "player_bans_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "player_bans_player_uuid_idx" ON "player_bans" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "username_search_idx" ON "players" USING gin (to_tsvector('english', "username"));