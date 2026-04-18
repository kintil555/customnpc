package com.npcmod.network;

import com.npcmod.NpcMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncNpcDataS2CPayload(int entityId, NbtCompound data) implements CustomPayload {

    public static final CustomPayload.Id<SyncNpcDataS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of(NpcMod.MOD_ID, "sync_npc_data"));

    public static final PacketCodec<RegistryByteBuf, SyncNpcDataS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, SyncNpcDataS2CPayload::entityId,
                    PacketCodecs.NBT_COMPOUND, SyncNpcDataS2CPayload::data,
                    SyncNpcDataS2CPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}