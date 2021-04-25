/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 *
 * Special thank you to the Suff and their mod Regeneration.
 * That mod taught me how to add ticking capabilities to entities and have them sync
 * https://github.com/WhoCraft/Regeneration
 */

package leaf.cosmere.cap.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import leaf.cosmere.constants.Manifestations.ManifestationTypes;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.manifestation.AManifestation;
import leaf.cosmere.network.Network;
import leaf.cosmere.network.packets.SyncPlayerSpiritwebMessage;
import leaf.cosmere.registry.ManifestationRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
    "The actual outlet of the power is not chosen by the practitioner, but instead is hardwritten into their Spiritweb"
    -Khriss
    https://coppermind.net/wiki/Ars_Arcanum#The_Alloy_of_Law
 */

public class SpiritwebCapability implements ISpiritweb
{


    //region Render stuff.
    final ItemStack activeStore = new ItemStack(Blocks.TORCH);
    final ItemStack activeTap = new ItemStack(Blocks.SOUL_TORCH);
    final ItemStack inactive = new ItemStack(Items.STICK);
    //endregion



    //Injection
    @CapabilityInject(ISpiritweb.class)
    public static final Capability<ISpiritweb> CAPABILITY = null;

    //detect if capability has been set up yet
    private boolean didSetup = false;

    private static final BitSet checker = new BitSet();

    private final LivingEntity livingEntity;

    public final Map<ManifestationTypes, BitSet> UNLOCKED_MANIFESTATIONS =
            Arrays.stream(ManifestationTypes.values())
                    .collect(Collectors.toMap(
                            Function.identity(),
                            type -> new BitSet()));

    //todo figure out how to do what I originally set out to do 🙃
    //Hemalurgic powers are never saved back to the capability for serialisation
    //instead, the relevant curio will re-apply them each time the player logs in
    public final Map<ManifestationTypes, BitSet> TEMPORARY_MANIFESTATIONS =
            Arrays.stream(ManifestationTypes.values())
                    .collect(Collectors.toMap(
                            Function.identity(),
                            type -> new BitSet()));

    public final Map<ManifestationTypes, int[]> MANIFESTATIONS_MODE =
            Arrays.stream(ManifestationTypes.values())
                    .collect(Collectors.toMap(
                            Function.identity(),
                            type -> new int[16]));

    private AManifestation selectedManifestation = ManifestationRegistry.NONE.get();

    //biochromatic breaths stored.
    //todo, figure out how the passive buff works
    private int biochromaticBreathStored = 0 ;

    //stormlight stored

    private int stormlightStored = 0 ;

    //metals ingested
    public final Map<Metals.MetalType, Integer> METALS_INGESTED =
            Arrays.stream(Metals.MetalType.values())
                    .collect(Collectors.toMap(Function.identity(),type -> 0));



    public SpiritwebCapability(LivingEntity ent)
    {
        this.livingEntity = ent;
    }


    @Nonnull
    public static LazyOptional<ISpiritweb> get(LivingEntity entity)
    {
        return entity instanceof LivingEntity ? entity.getCapability(SpiritwebCapability.CAPABILITY, null) : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();

        for (ManifestationTypes manifestationType : ManifestationTypes.values())
        {
            String manifestationTypeName = manifestationType.name().toLowerCase();

            nbt.putByteArray(manifestationTypeName + "_temp", TEMPORARY_MANIFESTATIONS.get(manifestationType).toByteArray());
            nbt.putByteArray(manifestationTypeName + "_unlocked", UNLOCKED_MANIFESTATIONS.get(manifestationType).toByteArray());
            nbt.putIntArray(manifestationTypeName + "_mode", MANIFESTATIONS_MODE.get(manifestationType));

        }
        nbt.putString("selected_power", selectedManifestation.getRegistryName().toString());


        nbt.putInt("stored_breaths", biochromaticBreathStored);
        nbt.putInt("stored_stormlight", stormlightStored);

        for (Metals.MetalType metalType : Metals.MetalType.values())
        {
            nbt.putInt(metalType.name().toLowerCase() + "_ingested", METALS_INGESTED.get(metalType));
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        for (ManifestationTypes manifestationType : ManifestationTypes.values())
        {
            String manifestationTypeName = manifestationType.name().toLowerCase();

            TEMPORARY_MANIFESTATIONS.put(manifestationType, BitSet.valueOf(nbt.getByteArray(manifestationTypeName + "_temp")));
            UNLOCKED_MANIFESTATIONS.put(manifestationType, BitSet.valueOf(nbt.getByteArray(manifestationTypeName + "_unlocked")));
            MANIFESTATIONS_MODE.put(manifestationType, nbt.getIntArray(manifestationTypeName + "_mode"));
        }
        selectedManifestation = ManifestationRegistry.fromID(nbt.getString("selected_power"));

        biochromaticBreathStored = nbt.getInt("stored_breaths");
        stormlightStored = nbt.getInt("stored_stormlight");
        for (Metals.MetalType metalType : Metals.MetalType.values())
        {
            METALS_INGESTED.put(metalType,nbt.getInt(metalType.name().toLowerCase() + "_ingested"));
        }
    }

    @Override
    public void tick()
    {
        if (!livingEntity.world.isRemote)
        {
            //Login setup
            if (!didSetup)
            {
                syncToClients(null);
                didSetup = true;
            }

            //skip checking all the other powers if we know there's only one on this entity
            if (hasSingleManifestation() && manifestationActive(selectedManifestation.getManifestationType(), selectedManifestation.getPowerID()))
            {
                selectedManifestation.tick(this);
                return;
            }


            //Tick
            for (int i = 0; i < 16; i++)
            {
                for (ManifestationTypes manifestationType : ManifestationTypes.values())
                {
                    //don't tick powers that the user doesn't have or are not active
                    if (hasManifestation(manifestationType, i) && manifestationActive(manifestationType, i))
                    {
                        manifestationType.getManifestation(i).tick(this);
                    }
                }
            }

            //tick metals
            if (livingEntity.ticksExisted % 1200 == 0)
            {
                //metals can't stay in your system forever, y'know?
                for (Metals.MetalType metalType : Metals.MetalType.values())
                {
                    Integer metalIngestAmount = METALS_INGESTED.get(metalType);
                    if (metalIngestAmount > 0)
                    {
                        //todo decide how and when we poison the user for eating metal, that sure ain't safe champ.


                        //todo, decide what's appropriate for reducing ingested metal amounts
                        METALS_INGESTED.put(metalType, metalIngestAmount - 1);
                    }
                }


            }

            //tick stormlight
            if (stormlightStored > 0 && livingEntity.ticksExisted % 100 == 0)
            {
                //todo decide what's appropriate for reducing stormlight
                //maybe reducing cost based on how many ideals they have sworn?
                stormlightStored--;
            }

        }
    }

    @Override
    public LivingEntity getLiving()
    {
        return livingEntity;
    }


    @Override
    public boolean isMistborn()
    {
        checker.clear();
        checker.or(UNLOCKED_MANIFESTATIONS.get(ManifestationTypes.ALLOMANCY));
        checker.or(TEMPORARY_MANIFESTATIONS.get(ManifestationTypes.ALLOMANCY));
        return checker.cardinality() >= 16;
    }

    @Override
    public boolean isFullFeruchemist()
    {
        checker.clear();
        checker.or(UNLOCKED_MANIFESTATIONS.get(ManifestationTypes.FERUCHEMY));
        checker.or(TEMPORARY_MANIFESTATIONS.get(ManifestationTypes.FERUCHEMY));
        return checker.cardinality() >= 16;
    }

    @Override
    public int getIngestedMetal(Metals.MetalType metalType)
    {
        return METALS_INGESTED.get(metalType);
    }

    @Override
    public boolean adjustIngestedMetal(Metals.MetalType metalType, int val, boolean doAdjust)
    {
        int ingestedMetal = getIngestedMetal(metalType);
        val = Math.min(ingestedMetal, val);

        if (ingestedMetal >= val)
        {
            if (doAdjust)
                METALS_INGESTED.put(metalType, ingestedMetal + val);

            return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderHUD(MatrixStack ms, ClientPlayerEntity playerEntity, ISpiritweb spiritweb)
    {
        Minecraft mc = Minecraft.getInstance();
        MainWindow mainWindow = mc.getMainWindow();

        int x = 10;
        int y = mainWindow.getScaledHeight() / 5;

        ItemStack stackToRender = inactive;

        List<AManifestation> availableManifestations = getAvailableManifestations();

        for (AManifestation manifestation : availableManifestations)
        {
            switch (manifestation.getManifestationType())
            {
                case ALLOMANCY:
                    break;
                case FERUCHEMY:

                    boolean active = manifestationActive(manifestation.getManifestationType(), manifestation.getPowerID());

                    int mode = getMode(manifestation.getManifestationType(), manifestation.getPowerID());
                    if (active && mode > 0)
                    {
                        stackToRender = activeTap;
                    }
                    else if (active && mode < 0)
                    {
                        stackToRender = activeStore;
                    }
                    break;
                case RADIANT:
                    break;
                case ELANTRIAN:
                    break;
                case AWAKENER:
                    break;
            }

            mc.getItemRenderer().renderItemAndEffectIntoGUI(stackToRender, x, y);

            mc.fontRenderer.drawStringWithShadow(ms, I18n.format(manifestation.translation().getString()), x + 18, y + 6, 0xFF4444);
            y += 10;
        }
    }

    @Override
    public void setSelectedManifestation(AManifestation manifestation)
    {
        selectedManifestation = manifestation;
    }

    public int getNumPowers()
    {
        return getNumPowers(false);
    }

    private int getNumPowers(boolean ignoreTemp)
    {
        int powers = 0;
        checker.clear();

        for (ManifestationTypes manifestationTypes : ManifestationTypes.values())
        {
            checker.or(UNLOCKED_MANIFESTATIONS.get(manifestationTypes));
            if (!ignoreTemp)
            {
                checker.or(TEMPORARY_MANIFESTATIONS.get(manifestationTypes));
            }
            powers += checker.cardinality();
        }
        return powers;
    }

    @Override
    public boolean hasManifestation(ManifestationTypes manifestationTypeID, int powerID)
    {
        return hasManifestation(manifestationTypeID, powerID, false);
    }

    @Override
    public boolean hasManifestation(ManifestationTypes manifestationTypeID, int powerID, boolean ignoreTemporaryPower)
    {
        boolean hasPower = UNLOCKED_MANIFESTATIONS.get(manifestationTypeID).get(powerID);

        if (!hasPower && !ignoreTemporaryPower)
        {
            hasPower = TEMPORARY_MANIFESTATIONS.get(manifestationTypeID).get(powerID);
        }

        return hasPower;
    }


    @Override
    public void giveManifestation(ManifestationTypes manifestationTypeID, int powerID)
    {
        UNLOCKED_MANIFESTATIONS.get(manifestationTypeID).set(powerID, true);
    }

    @Override
    public void giveTemporaryManifestation(ManifestationTypes manifestationTypeID, int powerID)
    {
        TEMPORARY_MANIFESTATIONS.get(manifestationTypeID).set(powerID, true);
        //update single power var
    }


    @Override
    public void removeManifestation(ManifestationTypes manifestationTypeID, int powerID)
    {
        UNLOCKED_MANIFESTATIONS.get(manifestationTypeID).clear(powerID);
    }

    @Override
    public void removeTemporaryManifestation(ManifestationTypes manifestationTypeID, int powerID)
    {
        TEMPORARY_MANIFESTATIONS.get(manifestationTypeID).clear(powerID);
    }

    @Override
    public boolean manifestationActive(ManifestationTypes manifestationType, int powerID)
    {
        int[] manifestationPowersModes = MANIFESTATIONS_MODE.get(manifestationType);

        if (manifestationType == ManifestationTypes.NONE || manifestationPowersModes.length == 0)
            return false;

        return manifestationPowersModes[powerID] != 0;
    }

    @Override
    public boolean selectedManifestationActive()
    {
        if (selectedManifestation == null)
        {
            return false;
        }

        return manifestationActive(selectedManifestation.getManifestationType(), selectedManifestation.getPowerID());
    }

    @Override
    public void deactivateCurrentManifestation()
    {
        MANIFESTATIONS_MODE.get(selectedManifestation.getManifestationType())[selectedManifestation.getPowerID()] = 0;
    }

    @Override
    public void deactivateManifestations()
    {
        for (ManifestationTypes manifestationTypes : ManifestationTypes.values())
        {
            Arrays.fill(MANIFESTATIONS_MODE.get(manifestationTypes), 0);
        }
    }

    @Override
    public void clearManifestations()
    {
        deactivateManifestations();
        for (ManifestationTypes manifestationTypes : ManifestationTypes.values())
        {
            UNLOCKED_MANIFESTATIONS.get(manifestationTypes).clear();
        }
    }

    @Override
    public List<AManifestation> getAvailableManifestations()
    {
        return getAvailableManifestations(false);
    }

    @Override
    public List<AManifestation> getAvailableManifestations(boolean ignoreTemporaryPower)
    {
        List<AManifestation> list = new ArrayList<AManifestation>();

        for (int i = 0; i < 16; i++)
        {
            for (ManifestationTypes manifestationTypes : ManifestationTypes.values())
            {
                if (hasManifestation(manifestationTypes, i, ignoreTemporaryPower))
                {
                    list.add(manifestationTypes.getManifestation(i));
                }
            }
        }

        return list;
    }

    @Override
    public AManifestation manifestation()
    {
        return selectedManifestation;
    }


    @Override
    public AManifestation manifestation(ManifestationTypes manifestationType, int powerID)
    {
        return manifestationType.getManifestation(powerID);
    }

    @Override
    public boolean hasSingleManifestation()
    {
        return getNumPowers() == 1;
    }

    @Override
    public String changeManifestation(int dir)
    {
        List<AManifestation> unlockedManifestations = getAvailableManifestations();

        if (selectedManifestation == ManifestationRegistry.NONE.get())
        {
            selectedManifestation = unlockedManifestations.get(0);
        }
        else
        {
            for (int index = 0; index < unlockedManifestations.size(); index++)
            {
                AManifestation manifestation = unlockedManifestations.get(index);
                if (selectedManifestation == manifestation)
                {
                    //found a match,
                    int selection = index;

                    selection += (dir < 0) ? 1 : -1;
                    selection = (selection + unlockedManifestations.size()) % unlockedManifestations.size();

                    selectedManifestation = unlockedManifestations.get(selection);
                    break;
                }

            }
        }
        return selectedManifestation.translation().getKey();
    }

    @Override
    public void setMode(ManifestationTypes manifestationTypeID, int powerID, int mode)
    {
        MANIFESTATIONS_MODE.get(manifestationTypeID)[powerID] = mode;
    }

    @Override
    public int getMode(ManifestationTypes manifestationTypeID, int powerID)
    {
        return MANIFESTATIONS_MODE.get(manifestationTypeID)[powerID];
    }

    @Override
    public int nextMode(ManifestationTypes manifestationType, int powerID)
    {

        AManifestation aim = manifestationType.getManifestation(powerID);

        int currentMode = getMode(manifestationType, powerID);

        if (aim.modeWraps(this) && currentMode == aim.modeMax(this))
        {
            int modeMin = aim.modeMin(this);
            this.setMode(manifestationType, powerID, modeMin);
            return modeMin;
        }

        int mode = Math.min(currentMode + 1, aim.modeMax(this));
        this.setMode(manifestationType, powerID, mode);

        return mode;
    }

    @Override
    public int previousMode(ManifestationTypes manifestationType, int powerID)
    {
        AManifestation aim = manifestationType.getManifestation(powerID);

        int currentMode = getMode(manifestationType, powerID);

        if (aim.modeWraps(this) && currentMode == aim.modeMin(this))
        {
            int modeMax = aim.modeMax(this);
            this.setMode(manifestationType, powerID, modeMax);
            return modeMax;
        }

        int mode = Math.max(currentMode - 1, aim.modeMin(this));
        this.setMode(manifestationType, powerID, mode);
        return mode;
    }

    @Override
    public void syncToClients(@Nullable ServerPlayerEntity serverPlayerEntity)
    {
        if (livingEntity != null && livingEntity.world.isRemote)
        {
            throw new IllegalStateException("Don't sync client -> server");
        }

        CompoundNBT nbt = serializeNBT();

        if (serverPlayerEntity == null)
        {
            Network.sendToAllInWorld(new SyncPlayerSpiritwebMessage(this.livingEntity.getEntityId(), nbt), (ServerWorld) livingEntity.world);
        }
        else
        {
            Network.sendTo(new SyncPlayerSpiritwebMessage(this.livingEntity.getEntityId(), nbt), serverPlayerEntity);
        }
    }
}
