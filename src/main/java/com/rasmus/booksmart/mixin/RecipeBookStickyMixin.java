package com.rasmus.booksmart.mixin;

import com.rasmus.booksmart.CraftAll;
import com.rasmus.booksmart.BookSmart;
import com.rasmus.booksmart.ModeStrip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Grouped recipe buttons (all wooden slabs share one button) show and
 * send whichever variant a global 1.5 second clock points at, so a
 * click series silently swaps material when the clock ticks over. This
 * remembers the recipe you actually placed; the one button owning it
 * sticks to that variant (see RecipeButtonStickyMixin) while every
 * other button keeps the vanilla discovery rotation. The memory clears
 * when the crafting grid empties, so browsing is untouched.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookStickyMixin {

    @Shadow
    @Final
    protected RecipeBookMenu menu;

    @Shadow
    @Final
    private GhostSlots ghostSlots;

    // The grid stays empty client side until the server's slot update
    // lands, so an empty grid may only release the sticky once the grid
    // has been seen filled, with a short grace window for the reply.
    @Unique
    private boolean booksmart$armed;
    @Unique
    private int booksmart$graceTicks;

    /**
     * A vanilla placement always clears the grid first, which throws the
     * materials on the ground when the inventory is full in creative, and
     * is refused outright in survival. When the grid already holds the
     * clicked recipe (the sticky proves it) with a result ready, skip the
     * placement entirely and let the craft-all loop drive the existing
     * grid; it only re-places once the grid is empty, which clears nothing.
     */
    @Inject(method = "tryPlaceRecipe", at = @At("HEAD"), cancellable = true)
    private void booksmart$resumeWithoutClearing(RecipeCollection recipeCollection, RecipeDisplayId recipe,
            boolean useMaxItems, CallbackInfoReturnable<Boolean> cir) {
        if (useMaxItems && BookSmart.craftAllEnabled()
                && this.menu instanceof AbstractCraftingMenu craftingMenu
                && recipe.equals(BookSmart.stickyRecipe)
                && !CraftAll.gridEmpty(craftingMenu)
                && craftingMenu.getResultSlot().hasItem()) {
            CraftAll.begin(craftingMenu, recipe, this.ghostSlots::clear);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tryPlaceRecipe", at = @At("RETURN"))
    private void booksmart$rememberPlaced(RecipeCollection recipeCollection, RecipeDisplayId recipe,
            boolean useMaxItems, CallbackInfoReturnable<Boolean> cir) {
        if (BookSmart.enabled() && cir.getReturnValueZ()) {
            BookSmart.stickyRecipe = recipe;
            this.booksmart$armed = false;
            this.booksmart$graceTicks = 20;
        }
        if (BookSmart.rememberEnabled() && cir.getReturnValueZ()
                && recipeCollection.getRecipes().size() > 1
                && Minecraft.getInstance().level != null) {
            var context = net.minecraft.world.item.crafting.display.SlotDisplayContext
                    .fromLevel(Minecraft.getInstance().level);
            for (var entry : recipeCollection.getRecipes()) {
                if (entry.id().equals(recipe)) {
                    BookSmart.remember(BookSmart.groupKey(recipeCollection.getRecipes(), context),
                            BookSmart.resultId(entry, context));
                    break;
                }
            }
        }
        if (cir.getReturnValueZ() && useMaxItems && BookSmart.craftAllEnabled()
                && this.menu instanceof AbstractCraftingMenu craftingMenu) {
            CraftAll.begin(craftingMenu, recipe, this.ghostSlots::clear);
        }
    }

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    protected abstract int getXOrigin();

    @Shadow
    protected abstract int getYOrigin();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void booksmart$renderModeStrip(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
            float a, CallbackInfo ci) {
        if (this.isVisible() && this.menu instanceof AbstractCraftingMenu) {
            ModeStrip.render(graphics, this.getXOrigin(), this.getYOrigin(), mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void booksmart$clickModeStrip(MouseButtonEvent event, boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir) {
        if (this.isVisible() && event.button() == 0 && this.menu instanceof AbstractCraftingMenu
                && ModeStrip.mouseClicked(event.x(), event.y(), this.getXOrigin(), this.getYOrigin())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void booksmart$releaseWhenGridEmpties(CallbackInfo ci) {
        if (this.isVisible()) {
            BookSmart.maybeShowIntro(Minecraft.getInstance());
        }
        if (BookSmart.stickyRecipe == null) {
            return;
        }
        StackedItemContents grid = new StackedItemContents();
        this.menu.fillCraftSlotsStackedContents(grid);
        if (!((StackedItemContentsAccessor) (Object) grid).booksmart$raw().amounts.isEmpty()) {
            this.booksmart$armed = true;
        } else if (this.booksmart$armed || --this.booksmart$graceTicks <= 0) {
            BookSmart.stickyRecipe = null;
            this.booksmart$armed = false;
        }
    }
}
