/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.effects.store;

import leaf.cosmere.helpers.EffectsHelper;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.effects.FeruchemyEffectBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;


public class TinStoreEffect extends FeruchemyEffectBase
{
    public TinStoreEffect(Metals.MetalType type, EffectType effectType)
    {
        super(type, effectType);
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier)
    {
        if (entityLivingBaseIn.world.isRemote || entityLivingBaseIn.ticksExisted % 20 != 0)
        {
            return;
        }
        entityLivingBaseIn.addPotionEffect(EffectsHelper.getNewEffect(Effects.BLINDNESS, amplifier));
    }
}
