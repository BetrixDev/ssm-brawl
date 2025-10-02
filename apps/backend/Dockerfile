FROM oven/bun:latest AS build

WORKDIR /app

COPY package.json ./
RUN bun install

COPY . ./

ENV NODE_ENV=production

RUN bun run build

FROM gcr.io/distroless/base

WORKDIR /app

COPY --from=build /app/server server
COPY --from=build /app/src/db/migrations ./src/db/migrations

ENV NODE_ENV=production

CMD ["./server"]

EXPOSE 3000