# Version splits

One source tree, two jars (26.1/26.2 and 26.3). Add new finds here.

## 26.3 is a separate jar

`./gradlew build` produces `booksmart-X.Y.Z+26.1.jar` (26.1.x and 26.2). `./gradlew build -PmcTarget=26.3`
produces `booksmart-X.Y.Z+26.3.jar` (pins in `gradle-26.3.properties`). Files under `src/mc26_3/java` and
`src/mc26_3/resources` replace the same-named file of `src/main`; `src/mc26_3/remove.txt` lists main
files the 26.3 jar drops. `verifyMixinClasses` (part of `check`) fails when a mixins.json entry has no
class in that variant. Check each jar against the Minecraft versions it claims without starting the game:
`python C:\Users\Rasmus\.claude\skills\minecraft-mods\scripts\linkcheck.py <jar> <mapped minecraft jar>`
(plugin-gated twins report as missing on the other version; that is expected).

| What | 26.1/26.2 | 26.3 | Fix |
|---|---|---|---|
| Left mouse button | 0 | 1 (`InputConstants.MOUSE_BUTTON_LEFT`) | `MouseButtons.isLeft`, replaced in `src/mc26_3` |

Full 26.3 table: `C:\Users\Rasmus\Desktop\Ny mappe (3)\mc-26.3-splits.md`.
