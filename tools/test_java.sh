#!/bin/sh
# Tests autonomes : JDK 21, ou ECJ_JAR et JAVA pour un compilateur Eclipse externe.
set -eu
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT HUP INT TERM
cd "$root"
if [ "${ECJ_JAR:-}" ]; then
  "${JAVA:-java}" -jar "$ECJ_JAR" -21 -encoding UTF-8 -d "$tmp" src/main/java/org/cambocraft/power/Gacha.java src/main/java/org/cambocraft/power/Rarity.java src/main/java/org/cambocraft/power/PityState.java src/main/java/org/cambocraft/power/PowerCatalog.java src/main/java/org/cambocraft/power/PowerProgress.java src/main/java/org/cambocraft/power/NenState.java src/main/java/org/cambocraft/power/NenJournal.java src/main/java/org/cambocraft/power/NenStateCodec.java src/main/java/org/cambocraft/power/NenArchive.java src/main/java/org/cambocraft/world/*.java tests/java/org/cambocraft/power/*.java tests/java/org/cambocraft/world/*.java tools/java/org/cambocraft/power/*.java
else
  javac --release 21 -encoding UTF-8 -d "$tmp" src/main/java/org/cambocraft/power/Gacha.java src/main/java/org/cambocraft/power/Rarity.java src/main/java/org/cambocraft/power/PityState.java src/main/java/org/cambocraft/power/PowerCatalog.java src/main/java/org/cambocraft/power/PowerProgress.java src/main/java/org/cambocraft/power/NenState.java src/main/java/org/cambocraft/power/NenJournal.java src/main/java/org/cambocraft/power/NenStateCodec.java src/main/java/org/cambocraft/power/NenArchive.java src/main/java/org/cambocraft/world/*.java tests/java/org/cambocraft/power/*.java tests/java/org/cambocraft/world/*.java tools/java/org/cambocraft/power/*.java
fi
"${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.GachaTest
"${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.NenJournalTest
"${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.NenStateCodecTest
"${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.world.SeasonClockTest
if [ "${1:-}" = "--simulate" ]; then
  "${JAVA:-java}" -Dfile.encoding=UTF-8 -cp "$tmp" org.cambocraft.power.GachaSimulation 5000000 20261111
fi
