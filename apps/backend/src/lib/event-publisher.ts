import { EventPublisher } from "@orpc/server";

export const eventPublisher = new EventPublisher<{
  updatePlayerCount: {
    playerCount: number;
  };
}>();
