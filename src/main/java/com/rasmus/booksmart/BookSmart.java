package com.rasmus.booksmart;

import com.rasmus.booksmart.config.BookSmartConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

/** Shared sticky state and config access for the mixins. */
public final class BookSmart {

    /** The recipe variant last placed via the recipe book; the button owning it sticks to it. */
    public static volatile RecipeDisplayId stickyRecipe;

    private static ConfigHolder<BookSmartConfig> holder;

    private BookSmart() {
    }

    public static void init() {
        holder = AutoConfig.register(BookSmartConfig.class,
                me.shedaniel.autoconfig.serializer.GsonConfigSerializer::new);
    }

    public static boolean enabled() {
        return holder == null || holder.getConfig().stickyCrafting;
    }

    public static boolean craftAllEnabled() {
        return holder == null || holder.getConfig().craftAllOnShiftClick;
    }

    public static BookSmartConfig.DropMode dropMode() {
        return holder == null ? BookSmartConfig.DropMode.STOP : holder.getConfig().dropMode;
    }

    public static boolean rememberEnabled() {
        return holder != null && holder.getConfig().rememberVariants;
    }

    /**
     * Variant memory is keyed by the group's sorted result item ids, which
     * stay stable across sessions unlike the display ids; the value is the
     * result item of the variant last placed from that group.
     */
    public static String groupKey(java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplayEntry> recipes,
            net.minecraft.util.context.ContextMap resolutionContext) {
        java.util.TreeSet<String> ids = new java.util.TreeSet<>();
        for (var entry : recipes) {
            String id = resultId(entry, resolutionContext);
            if (id != null) {
                ids.add(id);
            }
        }
        return String.join(",", ids);
    }

    public static String resultId(net.minecraft.world.item.crafting.display.RecipeDisplayEntry entry,
            net.minecraft.util.context.ContextMap resolutionContext) {
        var items = entry.resultItems(resolutionContext);
        if (items.isEmpty()) {
            return null;
        }
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(items.get(0).getItem()).toString();
    }

    public static String preferredFor(String key) {
        return holder == null ? null : holder.getConfig().preferredVariants.get(key);
    }

    public static void remember(String key, String itemId) {
        if (holder == null || key.isEmpty() || itemId == null) {
            return;
        }
        if (!itemId.equals(holder.getConfig().preferredVariants.put(key, itemId))) {
            holder.save();
        }
    }

    private static final SystemToast.SystemToastId INTRO_TOAST = new SystemToast.SystemToastId();

    /** One-time hint the first time the recipe book is actually on screen. */
    public static void maybeShowIntro(Minecraft minecraft) {
        if (holder == null || holder.getConfig().introShown) {
            return;
        }
        holder.getConfig().introShown = true;
        holder.save();
        minecraft.getToastManager().addToast(SystemToast.multiline(minecraft, INTRO_TOAST,
                Component.translatable("booksmart.intro.title"),
                Component.translatable("booksmart.intro.message")));
    }
}
