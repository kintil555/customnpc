package com.npcmod;

import com.npcmod.client.entity.NpcEntityRenderer;
import com.npcmod.client.screen.NpcEditScreen;
import com.npcmod.network.SyncNpcDataS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class NpcModClient implements ClientModInitializer {

    public static volatile int lastOpenedNpcId = -1;
    public static volatile @Nullable NbtCompound lastOpenedNpcData = null;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.NPC, NpcEntityRenderer::new);

        HandledScreens.register(ModScreenHandlers.NPC_EDIT, NpcEditScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncNpcDataS2CPayload.ID, (payload, context) -> {
            lastOpenedNpcId = payload.entityId();
            lastOpenedNpcData = payload.data().copy();
        });
    }
}