export function useRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

// create a function to generate a random number that is 32 bits long
export function generateRandomInt32(): number {
  return Math.floor(Math.random() * 2 ** 32);
}
