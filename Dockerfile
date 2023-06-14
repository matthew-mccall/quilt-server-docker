# syntax=docker/dockerfile:1

FROM eclipse-temurin:17 AS installer

ARG MINECRAFT_VERSION=1.20
ARG MODS=ferrite-core,starlight,krypton,alternate-current,lithium,c2me-fabric,carpet
ARG DATAPACKS=""

RUN apt-get update && apt-get install -y curl

WORKDIR /install

COPY . .

RUN mkdir -p mods/

RUN ./scripts/installModsAndDataPacks.sh \
--minecraft-version ${MINECRAFT_VERSION} \
--datapacks ${DATAPACKS} \
--mods ${MODS} \
--server-root /install
RUN ./scripts/installQuiltServer.sh "${MINECRAFT_VERSION}"

# Download Mods and Data Packs
FROM ghcr.io/graalvm/graalvm-ce:latest

COPY --from=installer /install/world ./world
COPY --from=installer /install/mods ./mods
COPY --from=installer /install/libraries ./libraries
COPY --from=installer /install/*.jar ./

COPY ./server.properties .
COPY ./scripts/launch.sh ./scripts/launch.sh

CMD ["sh", "scripts/launch.sh"]