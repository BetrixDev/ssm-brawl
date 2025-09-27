FROM oven/bun:latest AS builder

WORKDIR /app

COPY package.json ./
RUN bun install

COPY . ./

RUN bun run build

FROM debian:bookworm-slim

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/server ./server

EXPOSE 3000/tcp

CMD ["./server"]