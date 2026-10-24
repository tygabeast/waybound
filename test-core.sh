#!/bin/sh
# Compiles and tests only the Minecraft-free core package with plain javac and JUnit.
# Used where Gradle cannot reach the NeoForge plugin repositories. On a normal machine use: gradle test
set -e
cd "$(dirname "$0")"
[ -f tools/junit-console.jar ] || { mkdir -p tools; curl -sS -o tools/junit-console.jar https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar; }
OUT=build/core-test
rm -rf "$OUT" && mkdir -p "$OUT"
javac -nowarn -parameters -d "$OUT" -cp tools/junit-console.jar \
  $(find src/main/java/com/tygabeast/waybound/core -name '*.java') \
  $(find src/test/java -name '*.java')
java -jar tools/junit-console.jar execute --class-path "$OUT" --scan-class-path --disable-banner --details=tree 2>&1 | grep -v JAVA_TOOL
