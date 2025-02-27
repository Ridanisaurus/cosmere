/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.effects.feruchemy.tap;

import leaf.cosmere.constants.Metals;
import leaf.cosmere.effects.feruchemy.FeruchemyEffectBase;
import leaf.cosmere.utils.helpers.EffectsHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;


public class IronTapEffect extends FeruchemyEffectBase
{
    public IronTapEffect(Metals.MetalType type, EffectType effectType)
    {
        super(type, effectType);
        addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                "bb29d10a-c58f-4f7e-956b-133b2685831f",
                0.1D,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier)
    {
        //ensure the user has correct buffs at least as strong as their store effect
        if (entityLivingBaseIn.level.isClientSide || entityLivingBaseIn.tickCount % 20 != 0)
        {
            return;
        }
        entityLivingBaseIn.addEffect(EffectsHelper.getNewEffect(Effects.SLOW_FALLING, -amplifier));
        entityLivingBaseIn.addEffect(EffectsHelper.getNewEffect(Effects.JUMP, -amplifier));
    }
}
