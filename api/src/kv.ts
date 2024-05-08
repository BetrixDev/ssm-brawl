import { createStorage, Storage } from "unstorage";

type KnownKeyValues = {
  isShuttingDown: boolean;
};

export const kv = createStorage() as Omit<Storage, "getItem" | "setItem"> & {
  getItem: <T extends keyof KnownKeyValues>(key: T) => Promise<KnownKeyValues[T] | null>;
  setItem: <T extends keyof KnownKeyValues>(key: T, value: KnownKeyValues[T]) => Promise<void>;
};
