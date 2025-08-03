# Build stage for the Gradle plugin
FROM gradle:8.10-jdk21 AS build

WORKDIR /app

# Copy gradle files
COPY ./build.gradle.kts ./
COPY ./settings.gradle.kts ./
COPY ./gradle.properties ./
COPY ./gradlew ./
COPY ./gradlew.bat ./
COPY ./gradle ./gradle

# Fix line endings and make gradlew executable
RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix ./gradlew && \
    chmod +x ./gradlew && \
    rm -rf /var/lib/apt/lists/*

# Download dependencies (optional optimization step)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY ./src ./src

# Build the plugin
RUN ./gradlew build --no-daemon

# Runtime stage with Minecraft server
FROM itzg/minecraft-server:latest

# Set server type to Leaf (Paper fork focused on performance)
ENV TYPE=LEAF
ENV VERSION=1.21.6

# Server configuration
ENV EULA=TRUE
ENV MAX_PLAYERS=20
ENV ANNOUNCE_PLAYER_ACHIEVEMENTS=FALSE
ENV SPAWN_PROTECTION=0
ENV ONLINE_MODE=${ONLINE_MODE:-TRUE}
ENV SPIGET_RESOURCES=81,6245
ENV OPS=8ebfb8be-0095-49ed-943e-64cc61220b40

# Performance and memory settings
ENV USE_AIKAR_FLAGS=TRUE
ENV MEMORY=${MEMORY_SIZE:-4G}
ENV ENABLE_ROLLING_LOGS=TRUE
ENV SNOOPER_ENABLED=FALSE
ENV MAX_TICK_TIME=-1

# RCON configuration  
ENV ENABLE_RCON=TRUE
ENV RCON_PASSWORD=${RCON_PASSWORD:-minecraft}
ENV RCON_PORT=25575

# Server port configuration
ENV SERVER_PORT=${MINECRAFT_PORT:-25565}

COPY ./server/ /data/

# Copy the built plugin from the build stage
# Use *.jar to catch any jar file, or specify the exact name if you know it
COPY --from=build /app/build/libs/*-all.jar /plugins/

# Ensure plugins directory has correct permissions
USER root
RUN chown -R minecraft:minecraft /plugins
USER minecraft