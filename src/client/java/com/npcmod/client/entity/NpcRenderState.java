package com.npcmod.client.entity;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class NpcRenderState extends LivingEntityRenderState {

    public float npcHeadPitch;
    public float npcHeadYaw;
    public float npcHeadRoll;
    public float npcBodyYaw;
    public float npcRightArmPitch;
    public float npcRightArmYaw;
    public float npcRightArmRoll;
    public float npcLeftArmPitch;
    public float npcLeftArmYaw;
    public float npcLeftArmRoll;
    public float npcRightLegPitch;
    public float npcRightLegYaw;
    public float npcRightLegRoll;
    public float npcLeftLegPitch;
    public float npcLeftLegYaw;
    public float npcLeftLegRoll;

    public Identifier skinTexture;

    public ItemStack mainHandStack = ItemStack.EMPTY;
    public ItemStack offHandStack = ItemStack.EMPTY;
}