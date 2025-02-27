/*
 * File created ~ 26 - 3 - 2022 ~ Leaf
 */

package leaf.cosmere.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.items.*;
import leaf.cosmere.items.curio.BraceletMetalmindItem;
import leaf.cosmere.items.curio.HemalurgicSpikeItem;
import leaf.cosmere.items.curio.NecklaceMetalmindItem;
import leaf.cosmere.items.curio.RingMetalmindItem;
import leaf.cosmere.registry.ItemsRegistry;
import leaf.cosmere.registry.LootFunctionRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RandomiseMetalTypeLootFunction extends LootFunction
{

    protected RandomiseMetalTypeLootFunction(ILootCondition[] conditionsIn)
    {
        super(conditionsIn);
    }


    @Override
    public LootFunctionType getType()
    {
        return LootFunctionRegistry.RANDOMISE_METALTYPE;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext lootContext)
    {
        final Item item = stack.getItem();
        if (!(item instanceof IHasMetalType))
        {
            return stack;
        }

        if (item instanceof HemalurgicSpikeItem)
        {
            List<Metals.MetalType> metalTypes =
                    Arrays.stream(Metals.MetalType.values())
                            .filter(Metals.MetalType::hasHemalurgicEffect)
                            .collect(Collectors.toList());
            Collections.shuffle(metalTypes);
            Optional<Metals.MetalType> metalType = metalTypes.stream().findFirst();

            if (metalType.isPresent())
            {
                stack = new ItemStack(ItemsRegistry.METAL_SPIKE.get(metalType.get()).get());
            }
        }
        else if (item instanceof MetalmindItem)
        {
            List<Metals.MetalType> metalTypes =
                    Arrays.stream(Metals.MetalType.values())
                            .filter(Metals.MetalType::hasFeruchemicalEffect)
                            .collect(Collectors.toList());
            Collections.shuffle(metalTypes);
            Optional<Metals.MetalType> metalType = metalTypes.stream().findFirst();

            if (metalType.isPresent())
            {
                Item metalmindItem;

                if (item instanceof RingMetalmindItem)
                {
                    metalmindItem = ItemsRegistry.METAL_RINGS.get(metalType.get()).get();
                }
                else if (item instanceof NecklaceMetalmindItem)
                {
                    metalmindItem = ItemsRegistry.METAL_NECKLACES.get(metalType.get()).get();
                }
                else if (item instanceof BraceletMetalmindItem)
                {
                    metalmindItem = ItemsRegistry.METAL_BRACELETS.get(metalType.get()).get();
                }
                else
                {
                    metalmindItem = item;
                }

                CompoundNBT nbt = stack.getOrCreateTag().copy();
                stack = new ItemStack(metalmindItem);
                stack.setTag(nbt);
            }
        }
        else if (item instanceof MetalRawOreItem)
        {
            List<Metals.MetalType> metalTypes =
                    Arrays.stream(Metals.MetalType.values())
                            .filter(Metals.MetalType::hasOre)
                            .collect(Collectors.toList());
            Collections.shuffle(metalTypes);
            Optional<Metals.MetalType> metalType = metalTypes.stream().findFirst();

            if (metalType.isPresent())
            {
                stack = new ItemStack(ItemsRegistry.METAL_RAW_ORE.get(metalType.get()).get(), stack.getCount());
            }
        }
        else if (item instanceof MetalNuggetItem)
        {
            List<Metals.MetalType> metalTypes =
                    Arrays.stream(Metals.MetalType.values())
                            .filter(Metals.MetalType::hasMaterialItem)
                            .collect(Collectors.toList());
            Collections.shuffle(metalTypes);
            Optional<Metals.MetalType> metalType = metalTypes.stream().findFirst();

            if (metalType.isPresent())
            {
                stack = new ItemStack(ItemsRegistry.METAL_NUGGETS.get(metalType.get()).get(), stack.getCount());
            }
        }
        else if (item instanceof MetalIngotItem)
        {
            List<Metals.MetalType> metalTypes =
                    Arrays.stream(Metals.MetalType.values())
                            .filter(Metals.MetalType::hasMaterialItem)
                            .collect(Collectors.toList());
            Collections.shuffle(metalTypes);
            Optional<Metals.MetalType> metalType = metalTypes.stream().findFirst();

            if (metalType.isPresent())
            {
                stack = new ItemStack(ItemsRegistry.METAL_INGOTS.get(metalType.get()).get(), stack.getCount());
            }
        }

        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<RandomiseMetalTypeLootFunction>
    {
        @Override
        public RandomiseMetalTypeLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext deserializationContext, ILootCondition[] lootConditions)
        {
            return new RandomiseMetalTypeLootFunction(lootConditions);
        }
    }
}