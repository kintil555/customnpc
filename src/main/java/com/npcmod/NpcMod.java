package com.npcmod;

import com.npcmod.network.SyncNpcDataS2CPayload;
import com.npcmod.network.UpdateNpcRotationC2SPayload;
import com.npcmod.network.UpdateNpcSkinC2SPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NpcMod implements ModInitializer {

    public static final String MOD_ID = "npcmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Map<UUID, Integer> PENDING_GUI_ENTITY = new HashMap<>();

    @Override
    public void onInitialize() {
        ModEntities.initialize();
        ModItems.initialize();
        ModScreenHandlers.initialize();

        PayloadTypeRegistry.playS2C().register(SyncNpcDataS2CPayload.ID, SyncNpcDataS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateNpcRotationC2SPayload.ID, UpdateNpcRotationC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateNpcSkinC2SPayload.ID, UpdateNpcSkinC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateNpcRotationC2SPayload.ID, (payload, context) -> {
            Entity e = context.player().getServerWorld().getEntityById(payload.entityId());
            if (e instanceof com.npcmod.entity.NpcEntity npc) {
                npc.applyRotationsFromNbt(payload.rotations());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateNpcSkinC2SPayload.ID, (payload, context) -> {
            Entity e = context.player().getServerWorld().getEntityById(payload.entityId());
            if (e instanceof com.npcmod.entity.NpcEntity npc) {
                npc.setSkinName(payload.skinName());
            }
        });
    }
}