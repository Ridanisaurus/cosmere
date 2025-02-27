/*
 * File created ~ 13 - 7 - 2021 ~ Leaf
 */

package leaf.cosmere.datagen.advancements;

import leaf.cosmere.registry.ItemsRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.TickTrigger;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class CoreAdvancements implements Consumer<Consumer<Advancement>>
{
    public CoreAdvancements()
    {
    }

    public void accept(Consumer<Advancement> advancementConsumer)
    {
        final String categoryName = "root";

        Advancement root = Advancement.Builder.advancement()
                .display(ItemsRegistry.GUIDE.get(),
                        new TranslationTextComponent("advancements.cosmere." + categoryName + ".title"),
                        new TranslationTextComponent("advancements.cosmere." + categoryName + ".description"),
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                        FrameType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("tick", new TickTrigger.Instance(EntityPredicate.AndPredicate.ANY))
                //.withRewards(new AdvancementRewards(0, new ResourceLocation[]{new ResourceLocation("cosmere:guide")}, new ResourceLocation[0], FunctionObject.CacheableFunction.EMPTY))
                .save(advancementConsumer, "core/" + categoryName);

        Advancement advancement1 = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        Items.WOODEN_PICKAXE,
                        new TranslationTextComponent("advancements.cosmere.mine_stone.title"),
                        new TranslationTextComponent("advancements.cosmere.mine_stone.description"),
                        (ResourceLocation) null,
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .addCriterion(
                        "get_stone",
                        InventoryChangeTrigger.Instance.hasItems(ItemPredicate.Builder.item().of(ItemTags.STONE_TOOL_MATERIALS).build()))
                .save(advancementConsumer, "core/mine_stone");

    }
}