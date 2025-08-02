# Docker Setup for Super Smash Mobs Brawl

This Docker Compose setup orchestrates all services required for the Super Smash Mobs Brawl project.

## Services

1. **PostgreSQL Database** - Data persistence for the server application
2. **Server** - Bun-powered TypeScript backend with tRPC and Better Auth
3. **Web** - Vite + React frontend application
4. **Minecraft** - Paper MC 1.21.6 server with the custom Kotlin plugin

## Prerequisites

- Docker and Docker Compose installed
- At least 4GB of available RAM (for Minecraft server)
- Ports 80, 3000, 5432, 25565, and 25575 available

## Quick Start

1. Copy the environment example file:
   ```bash
   cp .env.example .env
   ```

2. Update the `.env` file with your desired configuration

3. Build and start all services:
   ```bash
   docker compose up -d --build
   ```

4. Check service status:
   ```bash
   docker compose ps
   ```

## Service URLs

- **Web Application**: http://localhost
- **API Server**: http://localhost:3000
- **PostgreSQL**: localhost:5432
- **Minecraft Server**: localhost:25565
- **RCON**: localhost:25575

## Database Migrations

Run database migrations after the services are up:

```bash
docker compose exec server bun run db:migrate
```

## Coolify Deployment

This setup is optimized for Coolify deployment. For production:

1. Update the `.env` file with production values:
   ```env
   CORS_ORIGIN=https://your-domain.com
   VITE_SERVER_URL=https://api.your-domain.com
   POSTGRES_PASSWORD=strong-password-here
   RCON_PASSWORD=strong-rcon-password
   ```

2. In Coolify:
   - Set up a new Docker Compose project
   - Point to your repository
   - Configure environment variables
   - Enable automatic deployments on push to main branch

3. Configure domains in Coolify:
   - Web service: `your-domain.com`
   - Server service: `api.your-domain.com`
   - Minecraft: Use the server's IP with port 25565

## Monitoring

View logs for any service:
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f server
docker compose logs -f web
docker compose logs -f minecraft
docker compose logs -f postgres
```

## Minecraft Server Management

Connect to the Minecraft console:
```bash
docker attach ssmb-minecraft
```
(Use Ctrl+P, Ctrl+Q to detach without stopping)

Use RCON for remote management:
```bash
docker compose exec minecraft rcon-cli
```

## Backup

### Database Backup
```bash
docker compose exec postgres pg_dump -U postgres super-smash-mobs-brawl > backup.sql
```

### Minecraft World Backup
```bash
docker run --rm -v super-smash-mobs-brawl_minecraft_data:/data -v $(pwd):/backup alpine tar czf /backup/minecraft-world-backup.tar.gz -C /data .
```

## Troubleshooting

### Services not starting
- Check logs: `docker compose logs [service-name]`
- Ensure all required ports are free
- Verify environment variables are set correctly

### Database connection issues
- Ensure postgres service is healthy: `docker compose ps`
- Check DATABASE_URL in server environment
- Verify postgres credentials match in .env

### Minecraft plugin not loading
- Check plugin build: `docker compose logs minecraft | grep plugin`
- Ensure Paper MC version matches plugin compatibility
- Verify Java version compatibility (requires Java 21)

### Web app can't connect to server
- Verify VITE_SERVER_URL is correct
- Check CORS_ORIGIN matches the web app URL
- Ensure server is running and healthy

## Development

For local development with hot reload:

1. Start only the database:
   ```bash
   docker compose up postgres -d
   ```

2. Run services locally:
   ```bash
   # Terminal 1 - Server
   cd apps/server
   bun run dev

   # Terminal 2 - Web
   cd apps/web
   bun run dev

   # Terminal 3 - Minecraft plugin development
   cd plugin
   ./gradlew runServer
   ```

## Updating Services

To update a specific service after code changes:

```bash
# Rebuild and restart a specific service
docker compose up -d --build [service-name]

# Example: Update only the web app
docker compose up -d --build web
```

## Cleanup

Stop all services:
```bash
docker compose down
```

Remove all data (WARNING: This deletes all persistent data):
```bash
docker compose down -v
```