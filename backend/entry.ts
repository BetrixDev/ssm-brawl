import { app } from "./src";

app.listen(process.env.PORT ?? 1337, (server) => {
  console.log(`Backend is running at http://${server.hostname}:${server.port}`);
});
