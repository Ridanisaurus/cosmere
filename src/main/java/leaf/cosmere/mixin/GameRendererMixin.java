package leaf.cosmere.mixin;

import leaf.cosmere.constants.Metals;
import leaf.cosmere.registry.EffectsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(at = @At("RETURN"), method = "getNightVisionScale", cancellable = true)
    private static void removeNightVisionFlash(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> info)
    {
        EffectInstance effect = livingEntity.getEffect(Effects.NIGHT_VISION);
        float i = effect != null ? effect.getDuration() : f;
        info.setReturnValue(i > 0 ? 1 : i);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getFov",
            cancellable = true
    )
    private void getZoomedFov(ActiveRenderInfo activeRenderInfoIn, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> info)
    {
        double fov = info.getReturnValue();
        PlayerEntity player = (PlayerEntity)Minecraft.getInstance().getCameraEntity();
        EffectInstance tinTapEffect = player.getEffect(EffectsRegistry.TAPPING_EFFECTS.get(Metals.MetalType.TIN).get());
        EffectInstance tinStoreEffect = player.getEffect(EffectsRegistry.STORING_EFFECTS.get(Metals.MetalType.TIN).get());
        if (tinTapEffect != null && tinTapEffect.getDuration() > 0)
        {
            int amplifier = tinTapEffect.getAmplifier();
            double zoomedFov = fov / (amplifier + 2);
            info.setReturnValue(zoomedFov);
            return;
        }
        if (tinStoreEffect != null && tinStoreEffect.getDuration() > 0)
        {
            int amplifier = tinStoreEffect.getAmplifier();
            double zoomedFov = fov * (amplifier + 2);
            info.setReturnValue(zoomedFov);
            return;
        }

        info.setReturnValue(fov);
    }


}
