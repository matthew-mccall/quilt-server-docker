# syntax=docker/dockerfile:1

FROM eclipse-temurin:17 AS installer

ARG MINECRAFT_VERSION=1.20

RUN apt-get update && apt-get install -y curl

WORKDIR /install

COPY . .

RUN mkdir -p mods/

RUN ./scripts/bootstrapper/gradlew build --project-dir ./scripts/bootstrapper
RUN ./scripts/bootstrapper/gradlew run --project-dir ./scripts/bootstrapper --args="--minecraft-version ${MINECRAFT_VERSION} --datapacks terralith"

RUN ./scripts/installQuiltServer.sh "${MINECRAFT_VERSION}"

# Download Mods and Data Packs
FROM ghcr.io/graalvm/graalvm-ce:latest

COPY --from=installer /install/world .
COPY --from=installer /install/mods .
COPY --from=installer /install/libraries/ .
COPY --from=installer /install/*.jar .

COPY ./scripts/launch.sh ./scripts/launch.sh

CMD ["sh", "scripts/launch.sh"]