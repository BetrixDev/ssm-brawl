const LOG_LEVELS = {
  0: "Error",
  1: "Info",
} as const;

export type LogBody = {
  service: string;
  level: (typeof LOG_LEVELS)[keyof typeof LOG_LEVELS];
  message: string;
  time: number;
};

export class Logger {
  constructor(
    private service: string,
    private customHandler?: (payload: LogBody) => void
  ) {}

  info(message: string, meta?: any): void {
    this.log(message, 1, meta);
  }

  error(message: string, meta?: any) {
    this.log(message, 0, meta);
  }

  private log(message: string, level: keyof typeof LOG_LEVELS, meta?: any) {
    const base = {
      service: this.service,
      level: LOG_LEVELS[level],
      message,
      time: Date.now(),
    };

    if (this.customHandler !== undefined) {
      if (meta) {
        this.customHandler({ ...meta, ...base });
      } else {
        this.customHandler(base);
      }

      return;
    }

    if (meta) {
      console.log(JSON.stringify({ ...meta, ...base }));
    } else {
      console.log(JSON.stringify(base));
    }
  }
}
