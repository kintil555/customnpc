package com.npcmod.client.entity;

import com.npcmod.entity.NpcEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NpcEntityRenderer extends LivingEntityRenderer<NpcEntity, NpcRenderState, NpcEntityModel> {

    private static final Identifier STEVE_TEXTURE =
            Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    private final ItemModelManager itemModelManager;

    public NpcEntityRenderer(EntityRendererFactory.Context context) {
        super(context,
                new NpcEntityModel(NpcEntityModel.getTexturedModelData().createModel()),
                0.5f);
        this.itemModelManager = context.getItemModelManager();
        this.addFeature(new HeldItemFeatureRenderer<>(this, itemModelManager));
    }

    @Override
    public NpcRenderState createRenderState() {
        return new NpcRenderState();
    }

    @Override
    public void updateRenderState(NpcEntity entity, NpcRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        state.npcHeadPitch     = entity.getNpcHeadPitch();
        state.npcHeadYaw       = entity.getNpcHeadYaw();
        state.npcHeadRoll      = entity.getNpcHeadRoll();
        state.npcBodyYaw       = entity.getNpcBodyYaw();
        state.npcRightArmPitch = entity.getNpcRightArmPitch();
        state.npcRightArmYaw   = entity.getNpcRightArmYaw();
        state.npcRightArmRoll  = entity.getNpcRightArmRoll();
        state.npcLeftArmPitch  = entity.getNpcLeftArmPitch();
        state.npcLeftArmYaw    = entity.getNpcLeftArmYaw();
        state.npcLeftArmRoll   = entity.getNpcLeftArmRoll();
        state.npcRightLegPitch = entity.getNpcRightLegPitch();
        state.npcRightLegYaw   = entity.getNpcRightLegYaw();
        state.npcRightLegRoll  = entity.getNpcRightLegRoll();
        state.npcLeftLegPitch  = entity.getNpcLeftLegPitch();
        state.npcLeftLegYaw    = entity.getNpcLeftLegYaw();
        state.npcLeftLegRoll   = entity.getNpcLeftLegRoll();

        String skinName = entity.getSkinName();
        state.skinTexture = (skinName != null && !skinName.isEmpty())
                ? NpcSkinManager.getSkin(skinName)
                : STEVE_TEXTURE;

        ArmedEntityRenderState.updateRenderState(entity, state, itemModelManager);
    }

    @Override
    public Identifier getTexture(NpcRenderState state) {
        return state.skinTexture != null ? state.skinTexture : STEVE_TEXTURE;
    }

    @Override
    public void render(NpcRenderState state,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, -1.501, 0.0);
        super.render(state, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
