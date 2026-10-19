# Book Smart

Client-only Fabric mod with recipe-book quality of life (sticky crafting variant, variant memory, craft all). Mod id `booksmart`, GitHub RasmusKD/book-smart, Modrinth YwTDUNBt, branch master. Config is AutoConfig-based (Cloth Config), Mod Menu optional.

Read the `minecraft-mods` skill first (`C:\Users\Rasmus\.claude\skills\minecraft-mods\SKILL.md`). It holds the cross-mod rules: dual-version builds, release flow, CurseForge/Modrinth, testing in instances, config and icon style. Memory note for this mod: `booksmart-mod.md`.

## Build

- Java 25: `JAVA_HOME=C:\Users\Rasmus\.jdks\jdk-25.0.3+9`, then `./gradlew build`. The jar is written to `build/libs/`.
- No unit tests; the check is a clean build plus a run in a Modrinth App instance (see the skill).
- Two jars: `./gradlew build` (26.1.x and 26.2, `booksmart-X.Y.Z+26.1.jar`) and `./gradlew build -PmcTarget=26.3` (`+26.3`); see `VERSIONS.md`. The 26.1 and 26.2 versions differ in the toast manager and screen setters; resolve by name through reflection, and compile against 26.2 with `-Pminecraft_version=26.2 -Pfabric_version=0.158.0+26.2` before claiming support.
- Container clicks carry a state id: at most one click per tick, or the server ignores them and the craft loop stalls.

## Release

No workflow. Publish by hand through the Modrinth API (recipe in the memory note). PATCH `client_side`/`server_side` last and verify with a GET. Do not publish unless Rasmus asks.

## Keep skills and docs current (same rule as Nordvec)

When you learn something new, update the doc the next agent reads in the same change: this file for build and release facts, `README.md` for user-facing behavior, and the `minecraft-mods` skill for cross-mod lessons. Correct or delete outdated text instead of adding a contradicting note. Then run `python C:\Users\Rasmus\.claude\skills\minecraft-mods\scripts\check_citations.py`.

No em or en dashes in any text. Commit messages describe the code change.
