import { envsafe, str } from "envsafe";

export const env = envsafe({
  API_AUTH_TOKEN: str(),
  DB_CONNECTION_STRING: str(),
  DATABASE_HOST: str(),
  DATABASE_USERNAME: str(),
  DATABASE_PASSWORD: str(),
});
