import { generateRandomInteger } from "oslo/crypto";

export function useRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

export function useRandomId(length: number) {
  let out = "";

  for (let i = 0; i < length; i++) {
    out += generateRandomInteger(10);
  }

  return out;
}
