package com.npcmod.client.entity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

public class NpcEntityModel extends EntityModel<NpcRenderState> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public NpcEntityModel(ModelPart root) {
        super(root);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8, 8, 8)
                        .uv(32, 0).cuboid(-4.0f, -8.0f, -4.0f, 8, 8, 8,
                                new net.minecraft.client.model.Dilation(0.5f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        root.addChild("body",
                ModelPartBuilder.create()
                        .uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8, 12, 4)
                        .uv(16, 32).cuboid(-4.0f, 0.0f, -2.0f, 8, 12, 4,
                                new net.minecraft.client.model.Dilation(0.25f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        root.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(40, 16).cuboid(-3.0f, -2.0f, -2.0f, 4, 12, 4)
                        .uv(40, 32).cuboid(-3.0f, -2.0f, -2.0f, 4, 12, 4,
                                new net.minecraft.client.model.Dilation(0.25f)),
                ModelTransform.pivot(-5.0f, 2.0f, 0.0f));

        root.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(32, 48).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4, 12, 4)
                        .uv(48, 48).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4, 12, 4,
                                new net.minecraft.client.model.Dilation(0.25f)),
                ModelTransform.pivot(5.0f, 2.0f, 0.0f));

        root.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4, 12, 4)
                        .uv(0, 32).cuboid(-2.0f, 0.0f, -2.0f, 4, 12, 4,
                                new net.minecraft.client.model.Dilation(0.25f)),
                ModelTransform.pivot(-1.9f, 12.0f, 0.0f));

        root.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(16, 48).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4, 12, 4)
                        .uv(0, 48).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4, 12, 4,
                                new net.minecraft.client.model.Dilation(0.25f)),
                ModelTransform.pivot(1.9f, 12.0f, 0.0f));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(NpcRenderState state) {
        head.pitch = state.npcHeadPitch;
        head.yaw   = state.npcHeadYaw;
        head.roll  = state.npcHeadRoll;

        body.yaw   = state.npcBodyYaw;

        rightArm.pitch = state.npcRightArmPitch;
        rightArm.yaw   = state.npcRightArmYaw;
        rightArm.roll  = state.npcRightArmRoll;

        leftArm.pitch = state.npcLeftArmPitch;
        leftArm.yaw   = state.npcLeftArmYaw;
        leftArm.roll  = state.npcLeftArmRoll;

        rightLeg.pitch = state.npcRightLegPitch;
        rightLeg.yaw   = state.npcRightLegYaw;
        rightLeg.roll  = state.npcRightLegRoll;

        leftLeg.pitch = state.npcLeftLegPitch;
        leftLeg.yaw   = state.npcLeftLegYaw;
        leftLeg.roll  = state.npcLeftLegRoll;
    }

    public ModelPart getHead()     { return head; }
    public ModelPart getBody()     { return body; }
    public ModelPart getRightArm() { return rightArm; }
    public ModelPart getLeftArm()  { return leftArm; }
    public ModelPart getRightLeg() { return rightLeg; }
    public ModelPart getLeftLeg()  { return leftLeg; }

    public ModelPart getHand(Arm arm) {
        return arm == Arm.RIGHT ? rightArm : leftArm;
    }
}