export async function getPlayerHeadSkinBase64(uuid: string) {
  try {
    const responsePng = await fetch(`https://mc-heads.net/avatar/${uuid}`).then((res) =>
      res.arrayBuffer(),
    );

    const base64 = Buffer.from(responsePng).toString("base64");

    return base64;
  } catch (error) {
    console.error(error);
    return null;
  }
}
