package com.npcmod.network;

import com.npcmod.NpcMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateNpcSkinC2SPayload(int entityId, String skinName) implements CustomPayload {

    public static final CustomPayload.Id<UpdateNpcSkinC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of(NpcMod.MOD_ID, "update_npc_skin"));

    public static final PacketCodec<RegistryByteBuf, UpdateNpcSkinC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, UpdateNpcSkinC2SPayload::entityId,
                    PacketCodecs.STRING, UpdateNpcSkinC2SPayload::skinName,
                    UpdateNpcSkinC2SPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}