const LOG_LEVELS = {
  0: "Error",
  1: "Info",
} as const;

export type LogBody = {
  service: string;
  level: (typeof LOG_LEVELS)[keyof typeof LOG_LEVELS];
  message?: string;
};

export class Logger {
  constructor(
    private service: string,
    private customHandler?: (payload: LogBody) => void
  ) {}

  info(payload: { message: string; meta?: any } | string): void {
    if (typeof payload === "string") {
      this.log({ message: payload, level: 1 });
    } else {
      this.log({ message: payload.message, level: 1, meta: payload.meta });
    }
  }

  error(payload: { message: string; meta?: any } | string) {
    if (typeof payload === "string") {
      this.log({ message: payload, level: 0 });
    } else {
      this.log({ message: payload.message, level: 0, meta: payload.meta });
    }
  }

  private log({
    message,
    meta,
    level,
  }: {
    message: string;
    meta?: any;
    level: keyof typeof LOG_LEVELS;
  }) {
    const base = {
      service: this.service,
      level: LOG_LEVELS[level],
      message,
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
