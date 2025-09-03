import { app } from "./src";

const port = Number(process.env.PORT) || 1337;

app.listen({ port, hostname: "0.0.0.0" }, (server) => {
  console.log(`Backend is running at http://${server.hostname}:${server.port}`);
});
