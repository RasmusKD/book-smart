package com.rasmus.booksmart.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StackedItemContents.class)
public interface StackedItemContentsAccessor {

    @Accessor("raw")
    StackedContents<Holder<Item>> booksmart$raw();
}
