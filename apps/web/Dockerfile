# Use Bun's official image as base
FROM oven/bun:1 AS base
WORKDIR /app

# Install dependencies stage
FROM base AS deps
# Copy package files for dependency installation
COPY package.json bun.lock* ./
COPY apps/web/package.json ./apps/web/
# Install dependencies
RUN bun install

# Build stage
FROM base AS builder
# Copy dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules

# Copy source code
COPY . .

# Build the application
WORKDIR /app/apps/web
RUN bun run build

# Production stage
FROM base AS runner
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 web

# Copy built application
COPY --from=builder --chown=web:nodejs /app/apps/web/.output ./.output

# Switch to non-root user
USER web

# Expose port
EXPOSE 3001

# Set environment variables
ENV NODE_ENV=production
ENV PORT=3001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:$PORT || exit 1

# Start the application
CMD ["bun", "run", ".output/server/index.mjs"]