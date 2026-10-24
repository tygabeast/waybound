# Waybound: The Sacred Arts

A fan-made Minecraft 1.21.1 NeoForge mod based on a sacred arts progression system. Free and non-commercial. Not for publication without the original author's permission; terminology changes or the project comes down if he asks.

See the design document and the build spec for the full plan.

## 0.1.0 (Phase 0 skeleton and milestone 1)

- Gradle project (ModDevGradle, NeoForge 21.1.256, Java 21), mod id `waybound`, package `com.tygabeast.waybound`.
- `core` package, no Minecraft imports: `Rank` (power ladder, scale values, madra capacity, drift multiplier), `Attunement` (zero-sum percentage vector, nothing moves back into Pure), `Coherence` (the design's F, S, C formula), `AspectingCurve` (closed-form logistic aspecting).
- 15 unit tests, including the five coherence sanity checks and the aspecting times from the design.

## Building and testing

- Normal machine: `gradle build` and `gradle test`, `gradle runClient`, `gradle runServer`.
- Core tests only, without Gradle: `./test-core.sh` (uses plain javac and downloads the JUnit console jar once).
