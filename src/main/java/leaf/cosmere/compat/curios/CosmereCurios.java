/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.compat.curios;

import leaf.cosmere.constants.Metals;
import leaf.cosmere.items.curio.HemalurgicSpikeItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Optional;
import java.util.function.Predicate;

public class CosmereCurios
{

    public static ICurio createTestCurioProvider()
    {
        return new TestCurio();
    }


    public static Optional<ImmutableTriple<String, Integer, ItemStack>> getEquippedSpikeCurio(LivingEntity livingEntity, Metals.MetalType metalType)
    {
        Predicate<ItemStack> spikePredicate = stack ->
        {
            final boolean isSpike = stack.getItem() instanceof HemalurgicSpikeItem;
            return isSpike && ((HemalurgicSpikeItem) stack.getItem()).getMetalType() == metalType;
        };
        return CuriosApi.getCuriosHelper().findEquippedCurio(spikePredicate, livingEntity);
    }

    public static ItemStack getEquippedSpikeCurioStack(PlayerEntity player, Metals.MetalType metalType)
    {
        final Optional<ImmutableTriple<String, Integer, ItemStack>> curioSpike = getEquippedSpikeCurio(player, metalType);
        if(curioSpike.isPresent())
        {
            return curioSpike.get().getRight();
        }
        return ItemStack.EMPTY;
    }

}
