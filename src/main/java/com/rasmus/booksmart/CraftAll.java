package com.rasmus.booksmart;

import com.rasmus.booksmart.config.BookSmartConfig;
import com.rasmus.booksmart.mixin.StackedItemContentsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

/**
 * Drives shift-click crafting to completion. Vanilla's shift click fills
 * the grid for one result stack and stops; this loop quick-moves the
 * result (which drains the whole grid fill) and re-places the recipe
 * until no result appears within the patience window, the inventory is
 * full, or the screen changes. Each round trip needs the server's slot
 * sync, so the loop advances one step per client tick.
 */
public final class CraftAll {

    private static final int PATIENCE_TICKS = 10;
    private static final int MAX_ROUNDS = 400;

    private static AbstractCraftingMenu menu;
    private static RecipeDisplayId recipe;
    private static Runnable ghostClearer;
    private static int patience;
    private static int rounds;
    private static boolean placedAndWaiting;
    private static boolean throwMode;

    private CraftAll() {
    }

    public static void begin(AbstractCraftingMenu menu, RecipeDisplayId recipe, Runnable ghostClearer) {
        CraftAll.menu = menu;
        CraftAll.recipe = recipe;
        CraftAll.ghostClearer = ghostClearer;
        patience = PATIENCE_TICKS;
        rounds = MAX_ROUNDS;
        placedAndWaiting = true;
        throwMode = false;
    }

    public static boolean gridEmpty(AbstractCraftingMenu menu) {
        StackedItemContents grid = new StackedItemContents();
        menu.fillCraftSlotsStackedContents(grid);
        return ((StackedItemContentsAccessor) (Object) grid).booksmart$raw().amounts.isEmpty();
    }

    private static boolean gridEmpty() {
        return gridEmpty(menu);
    }

    public static void cancel() {
        menu = null;
        recipe = null;
        ghostClearer = null;
    }

    public static void tick(Minecraft minecraft) {
        if (menu == null) {
            return;
        }
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.player.containerMenu != menu) {
            cancel();
            return;
        }
        Slot result = menu.getResultSlot();
        if (result.hasItem()) {
            placedAndWaiting = false;
            // Container clicks carry a state id the server verifies, so
            // only one click may be sent per tick or the second one gets
            // ignored and the run stalls. In throw mode the quick move is
            // pointless anyway (the inventory is full), so throw directly.
            // Drop All throws every result without ever banking.
            if (throwMode || BookSmart.dropMode() == BookSmartConfig.DropMode.DROP_ALL) {
                if (BookSmart.holdShiftRequired() && !minecraft.hasShiftDown()) {
                    cancel();
                    return;
                }
                minecraft.gameMode.handleContainerInput(
                        menu.containerId, result.index, 1, ContainerInput.THROW, minecraft.player);
                patience = PATIENCE_TICKS;
                return;
            }
            minecraft.gameMode.handleContainerInput(
                    menu.containerId, result.index, 0, ContainerInput.QUICK_MOVE, minecraft.player);
            if (result.hasItem()) {
                // The quick move left the result behind: inventory full.
                // Either switch to throwing the results on the ground, or
                // stop here, per config. The first throw goes next tick.
                if (BookSmart.dropMode() != BookSmartConfig.DropMode.DROP_EXCESS
                        || (BookSmart.holdShiftRequired() && !minecraft.hasShiftDown())) {
                    cancel();
                    return;
                }
                throwMode = true;
                patience = PATIENCE_TICKS;
                return;
            }
            // Only continue while shift is still held: releasing it ends
            // the run after the current batch, so a misclick costs one
            // vanilla batch instead of the whole material stock. Only
            // re-place onto an empty grid: a placement that has to clear
            // leftovers can throw the materials on the ground in creative
            // mode, and materials must never be what gets dropped.
            if (--rounds <= 0 || (BookSmart.holdShiftRequired() && !minecraft.hasShiftDown()) || !gridEmpty()) {
                cancel();
                return;
            }
            minecraft.gameMode.handlePlaceRecipe(menu.containerId, recipe, true);
            placedAndWaiting = true;
            throwMode = false;
            patience = PATIENCE_TICKS;
        } else if (--patience <= 0) {
            // In the throw flow the grid drains without a replacement
            // being sent, so try one placement before giving up.
            if (!placedAndWaiting && (!BookSmart.holdShiftRequired() || minecraft.hasShiftDown()) && gridEmpty()) {
                minecraft.gameMode.handlePlaceRecipe(menu.containerId, recipe, true);
                placedAndWaiting = true;
                // Placing pulls materials out of the inventory, which can
                // free room, so let each batch try banking again before
                // falling back to throwing.
                throwMode = false;
                patience = PATIENCE_TICKS;
                return;
            }
            // The last placement produced nothing: out of materials. The
            // server answered it with a ghost recipe; clear that away.
            if (ghostClearer != null) {
                ghostClearer.run();
            }
            cancel();
        }
    }
}
