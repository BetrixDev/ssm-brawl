
import {customAlphabet} from "nanoid"

const numberNanoid = customAlphabet("1234567890")

export function useRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

export function useRandomId(length: number) {
  return numberNanoid(length)
}
