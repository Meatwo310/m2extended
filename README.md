# m2extended

JSON/HJSON Mindustry mod that adds a collection of QoL content expanding vanilla gameplay.

Metadata values in `mod.hjson` are provisional and should be confirmed before content is finalized.

## Release

Releases are created semi-automatically from GitHub Actions.

1. Update `version` in `mod.hjson` and merge the change into `main`.
2. Open **Actions > Release Mod > Run workflow** on the `main` branch.
3. Leave `version` empty to use `mod.hjson`, or enter the same version explicitly.
4. Keep `draft` enabled to review the generated release before publishing.

The workflow packages the mod as `m2extended-vX.Y.Z.zip`, creates tag `vX.Y.Z`, and attaches the zip to the GitHub Release.
