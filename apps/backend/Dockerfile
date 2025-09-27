FROM oven/bun:latest AS builder

WORKDIR /app

COPY package.json ./
RUN bun install

COPY . ./

RUN bun run build

FROM debian:bookworm-slim

WORKDIR /app

COPY --from=builder /app/server ./server

EXPOSE 3000/tcp

CMD ["./server"]