# syntax=docker/dockerfile:1

## deps: install pnpm deps in a Turborepo workspace (Node base)
FROM node:20-bookworm-slim AS deps
WORKDIR /app
RUN corepack enable

# Copy workspace manifests first for better Docker layer caching
COPY pnpm-lock.yaml pnpm-workspace.yaml package.json turbo.json ./
COPY backend/package.json backend/package.json

# Install only backend's dependencies using the workspace lockfile
RUN pnpm -C backend install --frozen-lockfile


## builder: add Bun and compile the backend to a single binary
FROM node:20-bookworm-slim AS builder
WORKDIR /app

# Install Bun
RUN npm install -g bun

# Bring in installed node_modules from deps stage for module resolution
COPY --from=deps /app/node_modules /app/node_modules
COPY --from=deps /app/backend/node_modules /app/backend/node_modules

# Copy backend sources
COPY backend/tsconfig.json backend/tsconfig.json
COPY backend/src backend/src
COPY backend/package.json backend/package.json

# Compile to a single native binary via the workspace script
RUN corepack enable && pnpm -C backend run build:compile


## runner: minimal image containing only the compiled binary (debug variant includes BusyBox)
FROM gcr.io/distroless/base-debian12:debug AS runner

WORKDIR /app

COPY --from=builder /app/backend/backend /app/backend

EXPOSE 1337
ENV NODE_ENV=production
CMD ["/app/backend"]

