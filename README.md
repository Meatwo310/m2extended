# m2extended

m2extended is a Java content mod for Mindustry that adds small quality-of-life
logistics blocks while staying close to vanilla progression.

## Requirements

- Mindustry `157` or later
- No mod dependencies

## Content

### Distribution

| Block | Unlock | Build cost | Notes |
| --- | --- | --- | --- |
| Lead Armored Conveyor | Conveyor, after Ground Zero | `copper/1`, `lead/1` | Cheap armored conveyor that rejects side input from non-conveyors. |
| Silicon Armored Conveyor | Titanium Conveyor | `copper/1`, `lead/1`, `titanium/1`, `silicon/1` | Armored conveyor with titanium conveyor speed. |
| Lead Directed Router | Router | `copper/3`, `lead/3` | One-way router that accepts back input and distributes to front, left, and right after a short delay. |
| Lead Right Directed Router | Lead Directed Router | `copper/3`, `lead/3` | One-way router that distributes back input to front and right after a short delay. |
| Lead Left Directed Router | Lead Directed Router | `copper/3`, `lead/3` | One-way router that distributes back input to front and left after a short delay. |
| Lead Directed Junction | Junction | `copper/3`, `lead/3` | One-way junction that moves back input forward and left input right after a short delay. |
| Silicon Router | Router | `copper/3`, `lead/2`, `silicon/1` | Router variant that immediately distributes input without an internal buffer. |
| Silicon Junction | Junction | `copper/3`, `lead/2`, `silicon/1` | Junction variant that immediately passes crossing input without an internal buffer. |
| Silicon Directed Router | Lead Directed Router | `copper/3`, `lead/3`, `silicon/3` | One-way instant router that accepts back input and distributes to front, left, and right. |
| Silicon Right Directed Router | Lead Right Directed Router | `copper/3`, `lead/3`, `silicon/3` | One-way instant router that distributes back input to front and right. |
| Silicon Left Directed Router | Lead Left Directed Router | `copper/3`, `lead/3`, `silicon/3` | One-way instant router that distributes back input to front and left. |
| Silicon Directed Junction | Lead Directed Junction | `copper/3`, `lead/3`, `silicon/3` | One-way instant junction that moves back input forward and left input right. |

### Liquid

| Block | Unlock | Build cost | Notes |
| --- | --- | --- | --- |
| Lead Plated Conduit | Conduit | `metaglass/1`, `lead/1` | Cheap plated conduit that rejects side input from non-conduits and does not leak. |
| Silicon Plated Conduit | Pulse Conduit | `titanium/2`, `metaglass/1`, `silicon/1` | Plated conduit with pulse conduit pressure and higher liquid capacity. |

Japanese names and descriptions are provided in
`bundles/bundle_ja.properties`.

## Repository Layout

- `template.mod.hjson` - Mindustry mod metadata template.
- `build/generated/modMetadata/mod.hjson` - generated Mindustry mod metadata.
- `src/` - Java content definitions.
- `sprites/blocks/` - block sprites used by the content definitions.
- `bundles/` - localization bundles.
- `.github/workflows/commitTest.yml` - builds the deploy jar on pushes and pull
  requests, and creates GitHub Releases from `v*` tag pushes.

## Local Packaging

The mod is distributed as a jar built by Gradle:

```sh
./gradlew jar
```

Install `build/libs/m2extendedDesktop.jar` through Mindustry's mod import menu,
or place it in the Mindustry mods directory while developing.

## Release

Releases are created by pushing version tags.

1. Update `version` in `build.gradle.kts`.
2. Merge the version change into `main`.
3. Create and push a matching tag, for example `v0.1.1`.

On `v*` tag pushes, `commitTest.yml` validates that the tag version matches
the generated `mod.hjson`, builds `build/libs/m2extended-<version>.jar`, and
attaches that jar to the GitHub Release.
