# Book Smart

Client-side Fabric mod. A smarter recipe book: it does what you mean, not what the rotation happened to show.

Grouped recipe buttons (like wooden slabs) cycle which variant they show every 1.5 seconds, and a click crafts whatever is displayed at that exact moment, so crafting a few oak slabs can silently swap your grid over to birch mid-series.

## Features

- **Sticky Crafting**: the variant you click stays selected on its button until the crafting grid is empty. Other buttons keep the vanilla discovery rotation.
- **Remember Variants**: your last crafted variant per button is remembered across sessions, so the slabs button gives you your wood on the first click.
- **Craft All**: shift click and keep holding shift to craft until the materials run out, results going to your inventory. Release shift to stop, so a misclick costs one vanilla batch. When the inventory overflows, choose: stop, bank what fits and drop the rest, or drop everything. Materials are never what gets dropped.

## Config

Cloth Config via Mod Menu: Sticky Crafting (on), Remember Variants (on), Craft All on Shift Click (on), When Crafting Overflows (Stop / Drop Excess / Drop All).

## Requirements

Fabric Loader, Fabric API, Cloth Config. Minecraft 26.1+. Client-side only.

## License

MIT
