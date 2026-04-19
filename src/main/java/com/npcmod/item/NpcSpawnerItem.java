package com.npcmod.item;

import com.npcmod.ModEntities;
import com.npcmod.entity.NpcEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

        // Spawn di atas blok yang diklik
        BlockPos clickedPos = context.getBlockPos();
        Direction face = context.getSide();
        BlockPos spawnPos = face == Direction.UP
                ? clickedPos.up()
                : clickedPos.offset(face);

        NpcEntity npc = ModEntities.NPC.create((ServerWorld) world, SpawnReason.SPAWN_ITEM_USE);
        if (npc == null) return ActionResult.FAIL;

        // Restore data NPC dari NBT item jika ada (hasil pickup)
        ItemStack stack = context.getStack();
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("NpcData")) {
                npc.applyRotationsFromNbt(nbt.getCompound("NpcData"));
                String skin = nbt.getCompound("NpcData").getString("skin_name");
                if (skin != null && !skin.isEmpty()) {
                    npc.setSkinName(skin);
                }
            }
        }

        npc.refreshPositionAndAngles(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                context.getHorizontalPlayerFacing().getOpposite().asRotation(),
                0.0f
        );
        world.spawnEntity(npc);

        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getStack().decrement(1);
        }
        return ActionResult.CONSUME;
    }
}
