FROM ubuntu:latest

# TODO: Build plugin and copy into plugins folder

# Install Java
RUN apt-get update && apt-get install -y openjdk-8-jdk

WORKDIR /server

COPY pufferfish.jar /server/pufferfish.jar
COPY pufferfish.yml /server/pufferfish.yml
COPY purpur.yml /server/purpur.yml
COPY ops.json /server/ops.json
COPY server.properties /server/server.properties
COPY eula.txt /server/eula.txt

COPY ssmb_worlds /server/ssmb_worlds

EXPOSE 25565
EXPOSE 25565/tcp
EXPOSE 25565/udp

CMD ["java", "-jar", "pufferfish.jar"]