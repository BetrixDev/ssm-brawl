# Build stage
FROM oven/bun:1 AS builder

WORKDIR /app

# Copy root package files
COPY package.json bun.lock ./
COPY turbo.json ./

# Copy web app files
COPY apps/web/package.json ./apps/web/
COPY apps/web/tsconfig.json ./apps/web/
COPY apps/web/vite.config.ts ./apps/web/
COPY apps/web/index.html ./apps/web/
COPY apps/web/components.json ./apps/web/

# Install dependencies
RUN bun install --frozen-lockfile

# Copy web source code
COPY apps/web/src ./apps/web/src

# Set build-time environment variable
ARG VITE_SERVER_URL
ENV VITE_SERVER_URL=$VITE_SERVER_URL

# Build the web app
WORKDIR /app/apps/web
RUN bun run build

# Production stage
FROM nginx:alpine

# Copy nginx configuration
COPY --from=builder /app/apps/web/dist /usr/share/nginx/html

# Create nginx config for SPA routing
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ { \
        expires 1y; \
        add_header Cache-Control "public, immutable"; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]