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

  info(payload: ({ message?: string } & any) | string): void {
    if (typeof payload === "string") {
      this.log(1, { message: payload });
    } else {
      this.log(1, payload);
    }
  }

  error(payload: ({ message?: string } & any) | string) {
    if (typeof payload === "string") {
      this.log(0, { message: payload });
    } else {
      this.log(0, payload);
    }
  }

  private log(
    level: keyof typeof LOG_LEVELS,
    data: {
      message?: string;
    } & any
  ) {
    const logPayload = {
      service: this.service,
      level: LOG_LEVELS[level],
      ...data,
    };

    if (this.customHandler !== undefined) {
      this.customHandler(logPayload);

      return;
    }

    console.log(JSON.stringify(logPayload));
  }
}
