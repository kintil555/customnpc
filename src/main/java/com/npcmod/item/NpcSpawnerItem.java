package com.npcmod.item;

import com.npcmod.ModEntities;
import com.npcmod.entity.NpcEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NpcSpawnerItem extends Item {

    public NpcSpawnerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockPos spawnPos = context.getBlockPos().offset(context.getSide());
        NpcEntity npc = ModEntities.NPC.create((ServerWorld) world, SpawnReason.SPAWN_ITEM_USE);
        if (npc == null) {
            return ActionResult.FAIL;
        }
        npc.refreshPositionAndAngles(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f, 0.0f
        );
        world.spawnEntity(npc);
        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getStack().decrement(1);
        }
        return ActionResult.CONSUME;
    }
}