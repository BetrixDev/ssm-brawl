# Build stage
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy gradle files
COPY plugin/build.gradle.kts ./
COPY plugin/settings.gradle.kts ./
COPY plugin/gradle.properties ./
COPY plugin/gradlew ./
COPY plugin/gradlew.bat ./
COPY plugin/gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY plugin/src ./src

# Build the plugin
RUN ./gradlew shadowJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /minecraft

# Install necessary tools
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Download Paper MC 1.21.6
RUN wget -O paper.jar https://api.papermc.io/v2/projects/paper/versions/1.21.6/builds/latest/downloads/paper-1.21.6.jar

# Copy the built plugin
COPY --from=builder /app/build/libs/*.jar plugins/

# Accept EULA
RUN echo "eula=true" > eula.txt

# Create server.properties with basic configuration
RUN echo "server-port=25565\n\
enable-rcon=true\n\
rcon.port=25575\n\
rcon.password=minecraft\n\
max-players=20\n\
motd=Super Smash Mobs Brawl Server\n\
online-mode=false" > server.properties

# Expose Minecraft port and RCON port
EXPOSE 25565 25575

# Start the server with optimized flags
CMD ["java", "-Xms2G", "-Xmx2G", \
     "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", \
     "-XX:MaxGCPauseMillis=200", "-XX:+UnlockExperimentalVMOptions", \
     "-XX:+DisableExplicitGC", "-XX:+AlwaysPreTouch", \
     "-XX:G1NewSizePercent=30", "-XX:G1MaxNewSizePercent=40", \
     "-XX:G1HeapRegionSize=8M", "-XX:G1ReservePercent=20", \
     "-XX:G1HeapWastePercent=5", "-XX:G1MixedGCCountTarget=4", \
     "-XX:InitiatingHeapOccupancyPercent=15", "-XX:G1MixedGCLiveThresholdPercent=90", \
     "-XX:G1RSetUpdatingPauseTimePercent=5", "-XX:SurvivorRatio=32", \
     "-XX:+PerfDisableSharedMem", "-XX:MaxTenuringThreshold=1", \
     "-jar", "paper.jar", "--nogui"]