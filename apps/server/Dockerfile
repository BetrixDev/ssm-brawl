# Build stage
FROM oven/bun:1 AS builder

WORKDIR /app

# Copy root workspace files
COPY package.json bun.lock turbo.json ./
COPY biome.json ./

# Copy all package.json files for workspace
COPY apps/server/package.json ./apps/server/
COPY apps/web/package.json ./apps/web/

# Install all dependencies (needed for workspace)
RUN bun install --frozen-lockfile

# Copy server specific files
COPY apps/server/tsconfig.json ./apps/server/
COPY apps/server/drizzle.config.ts ./apps/server/
COPY apps/server/src ./apps/server/src

# Build the server using tsdown
WORKDIR /app/apps/server
RUN bun run build

# Production stage
FROM oven/bun:1-slim

WORKDIR /app

# Install PostgreSQL client and curl for health checks
RUN apt-get update && \
    apt-get install -y postgresql-client curl && \
    rm -rf /var/lib/apt/lists/*

# Copy package files
COPY --from=builder /app/apps/server/package.json ./
COPY --from=builder /app/apps/server/drizzle.config.ts ./

# Copy built application
COPY --from=builder /app/apps/server/dist ./dist

# Copy only production dependencies
COPY --from=builder /app/node_modules ./node_modules

# Create a non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

USER nodejs

# Expose port
EXPOSE 3000

# Start the server
CMD ["bun", "run", "start"]