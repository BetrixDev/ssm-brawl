import { Column, Entity, ObjectId, ObjectIdColumn } from "typeorm";
import { MessageChannelMessage } from "../models/MessageChannelMessage.js";

@Entity()
export class MessageChannel {
  @Column()
  users: string[];

  @Column(() => MessageChannelMessage)
  messages: MessageChannelMessage[];

  constructor(users: string[], messages: MessageChannelMessage[] = []) {
    this.users = users;
    this.messages = messages;
  }
}
