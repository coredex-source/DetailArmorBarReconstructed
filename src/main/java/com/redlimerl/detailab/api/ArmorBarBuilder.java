package com.redlimerl.detailab.api;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.render.ArmorBarRenderManager;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.Level;

import java.util.function.Function;

public class ArmorBarBuilder {
    private Item[] armor;
    private Function<ItemStack, ArmorBarRenderManager> predicate;

    /**
     * Specifies the {@link Item} on which the armor bar will appear when equipped.
     * @throws IllegalStateException [armorItem] aren't {@link Item}.
     */
    public ArmorBarBuilder armor(Item... armorItem) {
        try {
            armor = armorItem;
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("This is not ArmorItem");
        }
    }

    /**
     * Specifies the render options for the Armor Bar.
     * @see ArmorBarRenderManager
     */
    public ArmorBarBuilder render(Function<ItemStack, ArmorBarRenderManager> renderManager) {
        predicate = renderManager;
        return this;
    }

    /**
     * Registers the Custom Armor Bar so that it can be displayed.
     * @throws IllegalStateException Not all items have been initialized. check out {@link #armor(Item...)}, {@link #render(Function)}
     */
    public void register() {
        try {
            CustomArmorBar armorBar = new CustomArmorBar(predicate);
            for (Item armorItem : armor) {
                DetailArmorBarAPI.staticArmorList.put(armorItem, armorBar);
            }
            if (armor.length != 0) {
                DetailArmorBar.LOGGER.info("Successfully registered '"+ BuiltInRegistries.ITEM.getKey(armor[0]) + (armor.length > 1 ? "' and "+(armor.length-1)+" more items" : "'") + "!");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Not all items have been initialized");
        }
    }
}
