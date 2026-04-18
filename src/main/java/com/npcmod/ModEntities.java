package com.npcmod;

import com.npcmod.entity.NpcEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEntities {

    public static final RegistryKey<EntityType<?>> NPC_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(NpcMod.MOD_ID, "npc"));

    public static final EntityType<NpcEntity> NPC = EntityType.Builder
            .<NpcEntity>create(NpcEntity::new, SpawnGroup.MISC)
            .dimensions(0.6f, 1.8f)
            .maxTrackingRange(10)
            .trackingTickInterval(3)
            .build(NPC_KEY);

    public static void initialize() {
        Registry.register(Registries.ENTITY_TYPE, NPC_KEY, NPC);
        FabricDefaultAttributeRegistry.register(NPC, NpcEntity.createNpcAttributes());
    }
}