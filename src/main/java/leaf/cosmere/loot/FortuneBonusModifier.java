/*
 * File created ~ 26 - 3 - 2022 ~ Leaf
 */

package leaf.cosmere.loot;

import com.google.gson.JsonObject;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.utils.helpers.LogHelper;
import leaf.cosmere.registry.EffectsRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;


//Thank you Curios for the example !
// modified to work with the chromium feruchemy effects.

public class FortuneBonusModifier extends LootModifier
{
    protected FortuneBonusModifier(ILootCondition[] conditions)
    {
        super(conditions);
    }

    @Nonnull
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        final String hasCosmereFortuneBonus = "HasCosmereFortuneBonus";

        ItemStack tool = context.getParamOrNull(LootParameters.TOOL);

        if (tool != null && (!tool.hasTag() || tool.getTag() == null || !tool.getTag().getBoolean(hasCosmereFortuneBonus)))
        {
            Entity entity = context.getParamOrNull(LootParameters.THIS_ENTITY);
            BlockState blockState = context.getParamOrNull(LootParameters.BLOCK_STATE);

            if (blockState != null && entity instanceof LivingEntity)
            {
                LivingEntity player = (LivingEntity) entity;

                EffectInstance storingLuckEffect = player.getEffect(EffectsRegistry.STORING_EFFECTS.get(Metals.MetalType.CHROMIUM).get());
                EffectInstance tappingLuckEffect = player.getEffect(EffectsRegistry.TAPPING_EFFECTS.get(Metals.MetalType.CHROMIUM).get());

                //bonus for tapping amplifier.
                int totalFortuneBonus = 0;
                if (tappingLuckEffect != null)
                {
                    totalFortuneBonus = tappingLuckEffect.getAmplifier() + 1;
                }

                //minus off the unlucky effect amplifier

                if (storingLuckEffect != null)
                {
                    totalFortuneBonus -= (storingLuckEffect.getAmplifier() + 1);
                }

                if (totalFortuneBonus != 0)
                {
                    ItemStack fakeTool = tool.isEmpty() ? new ItemStack(Items.BARRIER) : tool.copy();

                    fakeTool.getOrCreateTag().putBoolean(hasCosmereFortuneBonus, true);

                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(fakeTool);
                    enchantments.put(Enchantments.BLOCK_FORTUNE, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, fakeTool) + totalFortuneBonus);

                    EnchantmentHelper.setEnchantments(enchantments, fakeTool);

                    LootContext.Builder builder = new LootContext.Builder(context);
                    builder.withParameter(LootParameters.TOOL, fakeTool);

                    LootContext newContext = builder.create(LootParameterSets.BLOCK);
                    LootTable lootTable = context.getLevel().getServer().getLootTables().get(blockState.getBlock().getLootTable());

                    return lootTable.getRandomItems(newContext);
                }
            }
        }

        //otherwise return the context that was passed in. no modification needed.
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<FortuneBonusModifier>
    {
        public Serializer()
        {
            LogHelper.info("Fortune bonus modifier setting up");
        }

        public FortuneBonusModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions)
        {
            return new FortuneBonusModifier(conditions);
        }

        public JsonObject write(FortuneBonusModifier instance)
        {
            return this.makeConditions(instance.conditions);
        }
    }
}
