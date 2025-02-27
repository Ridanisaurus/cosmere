/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.effects.feruchemy.store;

import leaf.cosmere.constants.Metals;
import leaf.cosmere.effects.feruchemy.FeruchemyEffectBase;
import leaf.cosmere.utils.helpers.EffectsHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;


public class IronStoreEffect extends FeruchemyEffectBase
{
    public IronStoreEffect(Metals.MetalType type, EffectType effectType)
    {
        super(type, effectType);
        addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                "a8fade1f-573d-405d-9885-39da3906d5f6",
                -0.1D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier)
    {
        //ensure the user has correct buffs at least as strong as their store effect
        if (entityLivingBaseIn.level.isClientSide || entityLivingBaseIn.tickCount % 20 != 0)
        {
            return;
        }
        entityLivingBaseIn.addEffect(EffectsHelper.getNewEffect(Effects.SLOW_FALLING, amplifier));
        entityLivingBaseIn.addEffect(EffectsHelper.getNewEffect(Effects.JUMP, amplifier));
    }
}
