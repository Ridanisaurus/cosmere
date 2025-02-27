/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.effects.feruchemy.store;

import leaf.cosmere.constants.Metals;
import leaf.cosmere.effects.feruchemy.FeruchemyEffectBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

//connection aka ability for people to notice you
public class DuraluminStoreEffect extends FeruchemyEffectBase
{
    public DuraluminStoreEffect(Metals.MetalType type, EffectType effectType)
    {
        super(type, effectType);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingVisibilityEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onRenderNameplateEvent);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier)
    {
        return true;
    }

    public void onLivingVisibilityEvent(LivingEvent.LivingVisibilityEvent event)
    {
        EffectInstance effectInstance = event.getEntityLiving().getEffect(this);
        if (effectInstance != null && effectInstance.getDuration() > 0)
        {
            //at max strength and wearing no armor, you could stand a block or two away from a creeper and it wont see you.
            //walk right into it though, and it will blow up.
            event.modifyVisibility(1f / (effectInstance.getAmplifier() + 2));
        }
    }

    public void onRenderNameplateEvent(RenderNameplateEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
        {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) event.getEntity();

        EffectInstance effectInstance = livingEntity.getEffect(this);
        if (effectInstance != null && effectInstance.getDuration() > 0)
        {
            if (effectInstance.getAmplifier() > 2)
            {
                event.setResult(Event.Result.DENY);
            }

        }
    }
}
