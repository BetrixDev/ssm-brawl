import { createStorage, Storage } from "unstorage";

type KnownKeyValues = {
  isShuttingDown: boolean;
};

const DEFAULT_VALUES: KnownKeyValues = {
  isShuttingDown: false,
};

export const kv = createStorage() as Omit<Storage, "getItem" | "setItem"> & {
  getItem: <T extends keyof KnownKeyValues>(key: T) => Promise<KnownKeyValues[T] | null>;
  setItem: <T extends keyof KnownKeyValues>(key: T, value: KnownKeyValues[T]) => Promise<void>;
};

export async function loadDefaultKvValues() {
  for (const [key, value] of Object.entries(DEFAULT_VALUES)) {
    await kv.setItem(key as any, value);
  }
}
