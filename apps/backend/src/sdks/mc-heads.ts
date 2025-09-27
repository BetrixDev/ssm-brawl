export async function getPlayerHeadSkinBase64(uuid: string) {
  try {
    const response = await fetch(`https://mc-heads.net/avatar/${uuid}`);

    if (!response.ok) {
      console.warn("mc-heads returned non-OK response", response.status, response.statusText);
      return null;
    }

    const arrayBuffer = await response.arrayBuffer();
    const base64 = Buffer.from(arrayBuffer).toString("base64");

    return base64;
  } catch (error) {
    console.error("Failed to get player head:", error);
    return null;
  }
}
