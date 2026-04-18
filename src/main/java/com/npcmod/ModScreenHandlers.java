package com.npcmod;

import com.npcmod.screen.NpcEditScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {

    public static final RegistryKey<ScreenHandlerType<?>> NPC_EDIT_KEY =
            RegistryKey.of(RegistryKeys.SCREEN_HANDLER, Identifier.of(NpcMod.MOD_ID, "npc_edit"));

    public static final ScreenHandlerType<NpcEditScreenHandler> NPC_EDIT =
            new ScreenHandlerType<>(
                    (syncId, inv) -> new NpcEditScreenHandler(syncId, inv),
                    FeatureFlags.VANILLA_FEATURES
            );

    public static void initialize() {
        Registry.register(Registries.SCREEN_HANDLER, NPC_EDIT_KEY, NPC_EDIT);
    }
}