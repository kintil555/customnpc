package com.npcmod.screen;

import com.npcmod.ModScreenHandlers;
import com.npcmod.entity.NpcEntity;
import com.npcmod.entity.NpcInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class NpcEditScreenHandler extends ScreenHandler {

    public static final int NPC_SLOT_COUNT = 6;
    public static final int BACKGROUND_WIDTH = 300;
    public static final int BACKGROUND_HEIGHT = 222;

    private final NpcInventory npcInventory;
    private final @Nullable NpcEntity entity;

    public NpcEditScreenHandler(int syncId, PlayerInventory playerInventory, NpcEntity entity) {
        super(ModScreenHandlers.NPC_EDIT, syncId);
        this.entity = entity;
        this.npcInventory = entity.getNpcInventory();
        npcInventory.onOpen(playerInventory.player);
        buildSlots(playerInventory);
    }

    public NpcEditScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.NPC_EDIT, syncId);
        this.entity = null;
        this.npcInventory = new NpcInventory();
        buildSlots(playerInventory);
    }

    private void buildSlots(PlayerInventory playerInventory) {
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_HEAD,     8, 20));
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_CHEST,    8, 38));
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_LEGS,     8, 56));
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_FEET,     8, 74));
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_MAINHAND, 8, 92));
        this.addSlot(new Slot(npcInventory, NpcInventory.SLOT_OFFHAND,  8, 110));

        int playerInvX = 69;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        playerInvX + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, playerInvX + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (index < NPC_SLOT_COUNT) {
                if (!this.insertItem(stack, NPC_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.insertItem(stack, 0, NPC_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (entity != null) {
            npcInventory.onClose(player);
        }
    }

    public @Nullable NpcEntity getEntity() {
        return entity;
    }

    public NpcInventory getNpcInventory() {
        return npcInventory;
    }
}