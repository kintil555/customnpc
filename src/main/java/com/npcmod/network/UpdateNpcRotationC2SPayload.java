package com.npcmod.network;

import com.npcmod.NpcMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateNpcRotationC2SPayload(int entityId, NbtCompound rotations) implements CustomPayload {

    public static final CustomPayload.Id<UpdateNpcRotationC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of(NpcMod.MOD_ID, "update_npc_rotation"));

    public static final PacketCodec<RegistryByteBuf, UpdateNpcRotationC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, UpdateNpcRotationC2SPayload::entityId,
                    PacketCodecs.NBT_COMPOUND, UpdateNpcRotationC2SPayload::rotations,
                    UpdateNpcRotationC2SPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}