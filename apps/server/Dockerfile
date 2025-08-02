# Build stage
FROM oven/bun:1 AS builder

WORKDIR /app

# Copy root package files
COPY package.json bun.lock ./
COPY turbo.json ./

# Copy server app files
COPY apps/server/package.json ./apps/server/
COPY apps/server/tsconfig.json ./apps/server/
COPY apps/server/drizzle.config.ts ./apps/server/

# Install dependencies
RUN bun install --frozen-lockfile

# Copy server source code
COPY apps/server/src ./apps/server/src

# Build the server
WORKDIR /app/apps/server
RUN bun run build

# Production stage
FROM oven/bun:1-slim

WORKDIR /app

# Install PostgreSQL client for health checks
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Copy built application
COPY --from=builder /app/apps/server/dist ./dist
COPY --from=builder /app/apps/server/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/apps/server/node_modules ./apps/server/node_modules

# Expose port
EXPOSE 3000

# Start the server
CMD ["bun", "run", "start"]