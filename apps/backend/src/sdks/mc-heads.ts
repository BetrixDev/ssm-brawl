export async function getPlayerHeadSkinBase64(uuid: string) {
  try {
    const responsePng = await fetch(`https://mc-heads.net/avatar/${uuid}`).then((res) =>
      res.arrayBuffer(),
    );

    const uint8Array = new Uint8Array(responsePng);
    const binaryString = String.fromCharCode.apply(null, Array.from(uint8Array));
    const base64 = btoa(binaryString);

    return base64;
  } catch (error) {
    console.error("Failed to get player head:", error);
    return null;
  }
}
