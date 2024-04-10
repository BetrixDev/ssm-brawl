import { Column } from "typeorm";

export class MessageChannelMessage {
  @Column()
  author: string;

  @Column()
  content: string;

  @Column()
  timestamp: number;

  constructor(author: string, content: string) {
    this.author = author;
    this.content = content;
    this.timestamp = Date.now();
  }
}
