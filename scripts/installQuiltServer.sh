#!/bin/sh

installer="$(curl -LOJsw "%{filename_effective}" "https://quiltmc.org/api/v1/download-latest-installer/java-universal")"
java -jar "$installer" install server $1 --download-server --install-dir="$(pwd)"