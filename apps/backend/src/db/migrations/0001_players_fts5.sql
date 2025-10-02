CREATE VIRTUAL TABLE IF NOT EXISTS `players_fts` USING fts5(
  `username`,
  content='players',
  content_rowid='rowid'
);
--> statement-breakpoint
CREATE TRIGGER IF NOT EXISTS `players_ai` AFTER INSERT ON `players` BEGIN
  INSERT INTO `players_fts`(rowid, `username`) VALUES (new.rowid, new.`username`);
END;
--> statement-breakpoint
CREATE TRIGGER IF NOT EXISTS `players_ad` AFTER DELETE ON `players` BEGIN
  INSERT INTO `players_fts`(`players_fts`, rowid, `username`) VALUES ('delete', old.rowid, old.`username`);
END;
--> statement-breakpoint
CREATE TRIGGER IF NOT EXISTS `players_au` AFTER UPDATE OF `username` ON `players` BEGIN
  INSERT INTO `players_fts`(`players_fts`, rowid, `username`) VALUES ('delete', old.rowid, old.`username`);
  INSERT INTO `players_fts`(rowid, `username`) VALUES (new.rowid, new.`username`);
END;
--> statement-breakpoint
INSERT INTO `players_fts`(`players_fts`) VALUES ('rebuild');

