/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.items.curio;

import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.vertex.*;
import leaf.cosmere.client.renderer.wearables.*;
import leaf.cosmere.constants.Metals;
import leaf.cosmere.itemgroups.CosmereItemGroups;
import leaf.cosmere.items.MetalmindItem;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.*;

import java.awt.*;
import java.util.*;

public class BraceletMetalmindItem extends MetalmindItem
{
    public BraceletMetalmindItem(Metals.MetalType metalType)
    {
        super(metalType, CosmereItemGroups.METALMINDS);
    }
    private static final ResourceLocation METAL_TEXTURE = new ResourceLocation("cosmere", "textures/block/metal_block.png");
    private Object model;

    @Override
    public float getMaxChargeModifier()
    {
        return (5f / 9f);
    }

    @Override
    public boolean canRender(String identifier, int index, LivingEntity livingEntity, ItemStack stack)
    {
        return true;
    }

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack,
                       IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch, ItemStack stack)
    {
        //todo check if needed
        // ICurio.RenderHelper.translateIfSneaking(matrixStack, livingEntity);
        // ICurio.RenderHelper.rotateIfSneaking(matrixStack, livingEntity);

        if (!(this.model instanceof BraceletModel))
        {
            //todo set bracelet position?
            this.model = new BraceletModel<>();
        }

        BraceletModel bracelet = (BraceletModel<?>) this.model;

        Optional<SlotTypePreset> slotTypePreset = SlotTypePreset.findPreset(identifier);
        if (!slotTypePreset.isPresent())
        {
            return;
        }

        bracelet.renderMode = identifier;
        bracelet.renderIndex = index;

        switch (slotTypePreset.get())
        {
            case BODY:
            case BACK:
            case BRACELET:
            case HANDS:
            case RING:
            case BELT:
            case CHARM:
            case CURIO:
                //setup biped model stuff
                bracelet.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
                bracelet.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                //and have it follow body rotations
                ICurio.RenderHelper.followBodyRotations(livingEntity, (BipedModel<LivingEntity>) bracelet);
                break;
        }

        IVertexBuilder vertexBuilder = ItemRenderer.getFoilBuffer(renderTypeBuffer, bracelet.renderType(METAL_TEXTURE), false, false);

        Color col = getMetalType().getColor();
        bracelet.renderToBuffer(matrixStack,
                vertexBuilder,
                light,
                OverlayTexture.NO_OVERLAY,
                col.getRed() / 255f,
                col.getGreen() / 255f,
                col.getBlue() / 255f,
                1.0F);
    }
}
