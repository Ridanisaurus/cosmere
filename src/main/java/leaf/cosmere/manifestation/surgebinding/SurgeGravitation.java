/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.manifestation.surgebinding;

import leaf.cosmere.Cosmere;
import leaf.cosmere.constants.Roshar;
import leaf.cosmere.utils.helpers.LogHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = Cosmere.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SurgeGravitation extends SurgebindingBase
{
    public SurgeGravitation(Roshar.Surges surge)
    {
        super(surge);
    }

    //gravitational attraction


    //@SubscribeEvent
    public void onLivingHurtEvent(LivingHurtEvent event)
    {
        if (event.getSource().getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
            LogHelper.info(player.getName() + " has attacked a " + event.getEntityLiving().getName());
            //ISurgeState SState = player.getCapability(CosmereCapabilities.SURGE_STATE, null);

            //String activeSurges = SState.getActiveSurgeName();
            ItemStack itemInHand = player.getMainHandItem();

            //windrunners like Szeth could launch enemies into the air to die cruelly by fall damage
            if (false)//activeSurges.equals(Names.KnightOrders.WINDRUNNER) && itemInHand == null)
            {
                event.getEntityLiving().setPos(event.getEntityLiving().getX(), event.getEntityLiving().getY() + 0.1d, event.getEntityLiving().getZ());
                event.getEntityLiving().setOnGround(false);
                event.getEntityLiving().setJumping(true);

                event.getEntityLiving().setDeltaMovement(0, 5, 0);

                //IPlayerStats PS = player.getCapability(CosmereCapabilities.PLAYER_STATS, null);
                //PS.remSL(100);//remove some stormlight as if it costs to hit
                //PacketDispatcher.sendTo(new SyncSurgeData(player.getCapability(CosmereCapabilities.SURGE_STATE, null), player.getCapability(CosmereCapabilities.PLAYER_STATS, null)), (PlayerEntityMP) player);
            }
        }


    }
}
