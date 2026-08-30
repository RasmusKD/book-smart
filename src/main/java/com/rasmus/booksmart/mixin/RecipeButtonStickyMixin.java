package com.rasmus.booksmart.mixin;

import com.rasmus.booksmart.BookSmart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The button owning the last placed recipe shows and sends that exact
 * variant while it stays craftable; every other button keeps the vanilla
 * discovery rotation. Releases when the grid empties (tracked in
 * RecipeBookStickyMixin) or the material runs out.
 */
@Mixin(RecipeButton.class)
public abstract class RecipeButtonStickyMixin {

    @Shadow
    private RecipeCollection collection;

    @Unique
    private RecipeDisplayId booksmart$remembered;

    /** Resolve this group's remembered variant once per button assignment. */
    @Inject(method = "init", at = @At("TAIL"))
    private void booksmart$resolveRemembered(RecipeCollection collection, boolean isFiltering,
            RecipeBookPage page, ContextMap resolutionContext, CallbackInfo ci) {
        this.booksmart$remembered = null;
        var recipes = collection.getRecipes();
        if (!BookSmart.rememberEnabled() || recipes.size() < 2) {
            return;
        }
        String want = BookSmart.preferredFor(BookSmart.groupKey(recipes, resolutionContext));
        if (want == null) {
            return;
        }
        for (var entry : recipes) {
            if (want.equals(BookSmart.resultId(entry, resolutionContext))) {
                this.booksmart$remembered = entry.id();
                return;
            }
        }
    }

    @Unique
    private RecipeDisplayId booksmart$stickyHere() {
        if (!BookSmart.enabled()) {
            return null;
        }
        RecipeDisplayId sticky = BookSmart.stickyRecipe;
        if (sticky != null && this.collection.isCraftable(sticky)) {
            return sticky;
        }
        RecipeDisplayId remembered = this.booksmart$remembered;
        if (remembered != null && this.collection.isCraftable(remembered)) {
            return remembered;
        }
        return null;
    }

    @Inject(method = "getCurrentRecipe", at = @At("HEAD"), cancellable = true)
    private void booksmart$stickCurrentRecipe(CallbackInfoReturnable<RecipeDisplayId> cir) {
        RecipeDisplayId sticky = booksmart$stickyHere();
        if (sticky != null) {
            cir.setReturnValue(sticky);
        }
    }

    @Inject(method = "getDisplayStack", at = @At("HEAD"), cancellable = true)
    private void booksmart$stickDisplayStack(CallbackInfoReturnable<ItemStack> cir) {
        RecipeDisplayId sticky = booksmart$stickyHere();
        if (sticky == null) {
            return;
        }
        for (RecipeDisplayEntry entry : this.collection.getRecipes()) {
            if (entry.id().equals(sticky)) {
                var items = entry.resultItems(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
                if (!items.isEmpty()) {
                    cir.setReturnValue(items.get(0));
                }
                return;
            }
        }
    }
}
