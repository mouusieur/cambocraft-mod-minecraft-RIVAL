#!/bin/sh
# Tests autonomes : JDK 21, ou ECJ_JAR et JAVA pour un compilateur Eclipse externe.
set -eu
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT HUP INT TERM
cd "$root"
if [ "${ECJ_JAR:-}" ]; then
  "${JAVA:-java}" -jar "$ECJ_JAR" -21 -encoding UTF-8 -d "$tmp" src/main/java/org/cambocraft/power/*.java tests/java/org/cambocraft/power/*.java tools/java/org/cambocraft/power/*.java
else
  javac --release 21 -encoding UTF-8 -d "$tmp" src/main/java/org/cambocraft/power/*.java tests/java/org/cambocraft/power/*.java tools/java/org/cambocraft/power/*.java
fi
"${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.GachaTest
if [ "${1:-}" = "--simulate" ]; then
  "${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.GachaSimulation 5000000 20261111
fi
