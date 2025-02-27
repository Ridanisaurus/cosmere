/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.properties;

import leaf.cosmere.itemgroups.CosmereItemGroups;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;

import java.util.function.Supplier;

public class PropTypes
{
    public static class Blocks
    {
        public static final Supplier<Block.Properties> EXAMPLE = () -> Block.Properties.of(Material.GLASS).strength(2.0F, 6.0F).harvestLevel(0).harvestTool(ToolType.PICKAXE);

        public static final Supplier<Block.Properties> METAL = () -> Block.Properties.of(Material.METAL).strength(2.0F, 6.0F).harvestLevel(1).harvestTool(ToolType.PICKAXE);
    }

    public static class Items
    {

        public static final Supplier<Item.Properties> SHARDBLADE = () -> new Item.Properties()
                .tab(CosmereItemGroups.ITEMS)
                .stacksTo(1)
                .defaultDurability(0)
                .durability(0)
                .setNoRepair()
                .rarity(Rarity.EPIC);


        public static final Supplier<Item.Properties> ONE = () -> new Item.Properties().tab(CosmereItemGroups.ITEMS).stacksTo(1);

        public static final Supplier<Item.Properties> SIXTEEN = () -> new Item.Properties().tab(CosmereItemGroups.ITEMS).stacksTo(16);

        public static final Supplier<Item.Properties> SIXTY_FOUR = () -> new Item.Properties().tab(CosmereItemGroups.ITEMS).stacksTo(64);
    }
}
