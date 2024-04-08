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
  ) {
    // Not using the env package since we don't need strict validation for NODE_ENV here
    const isDevEnvOrNoEnv =
      process.env.NODE_ENV === "development" ||
      process.env.NODE_ENV === undefined;

    if (isDevEnvOrNoEnv && customHandler === undefined) {
      this.customHandler = (payload) => {
        console.log(JSON.stringify(payload));
      };
    }
  }

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
