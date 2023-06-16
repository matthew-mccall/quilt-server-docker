# quilt-server-docker [![Docker](https://github.com/matthew-mccall/quilt-server-docker/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/matthew-mccall/quilt-server-docker/actions/workflows/docker-publish.yml) 
A collection of scripts and tools to setup a modded Quilt server

**Disclaimer:** This only contains scripts and tools to download and install datapacks and/or mods. This does not include or intend to to distribute copies of such datapacks and/or mods.

By default the image sets up a ready to go, vanilla-compatible 1.20 quilt server with performance enchancing mods. To install mods, build the image using `--build-arg="MODS=mods,to,install"` where `mods,to,install` is a comma-separated list of slugs and/or project IDs for mods on Modrith (such as `sodium` for instance). Datapacks are installed with `--build-arg="DATAPACKS=datapacks,to,install"`. So far, only `terralith` for 1.20 is supported. To change the Minecraft version use `--build-arg="MINECRAFT_VERSION=1.20"`. Pre-built images for linux/amd64 and linux/arm64 that provide the default build are on the GitHub containter registry.
