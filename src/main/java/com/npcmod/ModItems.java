package com.npcmod;

import com.npcmod.item.NpcSpawnerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {

    public static final RegistryKey<Item> NPC_SPAWNER_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NpcMod.MOD_ID, "npc_spawner"));

    public static final NpcSpawnerItem NPC_SPAWNER = new NpcSpawnerItem(
            new Item.Settings().registryKey(NPC_SPAWNER_KEY).maxCount(16)
    );

    public static void initialize() {
        Registry.register(Registries.ITEM, NPC_SPAWNER_KEY, NPC_SPAWNER);
    }
}