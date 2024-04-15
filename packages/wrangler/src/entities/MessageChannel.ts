import {
  Column,
  Entity,
  ObjectId,
  ObjectIdColumn,
  PrimaryColumn,
} from "typeorm";
import { MessageChannelMessage } from "../models/MessageChannelMessage.js";

@Entity("message_channels")
export class MessageChannel {
  @ObjectIdColumn()
  _id!: string;

  @Column()
  users: string[];

  @Column(() => MessageChannelMessage)
  messages: MessageChannelMessage[];

  constructor(users: string[], messages: MessageChannelMessage[] = []) {
    this.users = users;
    this.messages = messages;
  }
}
