# m2extended

m2extended is a Java content mod for Mindustry that adds small quality-of-life
logistics blocks while staying close to vanilla progression.

## Requirements

- Mindustry `154` or later
- No mod dependencies

## Content

### Distribution

| Block | Unlock | Build cost | Notes |
| --- | --- | --- | --- |
| Lead Armored Conveyor | Conveyor, after Ground Zero | `copper/1`, `lead/1` | Cheap armored conveyor that rejects side input from non-conveyors. |
| Silicon Armored Conveyor | Titanium Conveyor | `copper/1`, `lead/1`, `titanium/1`, `silicon/1` | Armored conveyor with titanium conveyor speed. |

### Liquid

| Block | Unlock | Build cost | Notes |
| --- | --- | --- | --- |
| Lead Plated Conduit | Conduit | `metaglass/1`, `lead/1` | Cheap plated conduit that rejects side input from non-conduits and does not leak. |
| Silicon Plated Conduit | Pulse Conduit | `titanium/2`, `metaglass/1`, `silicon/1` | Plated conduit with pulse conduit pressure and higher liquid capacity. |

Japanese names and descriptions are provided in
`bundles/bundle_ja.properties`.

## Repository Layout

- `mod.hjson` - Mindustry mod metadata and Java entrypoint.
- `src/` - Java content definitions.
- `sprites/blocks/` - block sprites used by the content definitions.
- `bundles/` - localization bundles.
- `.github/workflows/build.yml` - packages the mod on pushes to `main` and
  manual workflow runs.
- `.github/workflows/release.yml` - creates tagged GitHub releases from the
  packaged mod.

## Local Packaging

The mod is distributed as a jar built by Gradle:

```sh
./gradlew jar
```

Install `build/libs/m2extendedDesktop.jar` through Mindustry's mod import menu,
or place it in the Mindustry mods directory while developing.

## Release

Releases are created from GitHub Actions.

1. Update `version` in `mod.hjson`.
2. Merge the version change into `main`.
3. Open **Actions > Release Mod > Run workflow** on the `main` branch.
4. Leave `version` empty to use the value from `mod.hjson`, or enter the same
   semantic version explicitly.
5. Keep `draft` enabled when you want to inspect the generated release before
   publishing.

The workflow validates that the requested version matches `mod.hjson`, creates
tag `vX.Y.Z`, builds `m2extendedDesktop.jar`, renames it to
`m2extended-vX.Y.Z.jar`, and attaches it to the GitHub Release.
