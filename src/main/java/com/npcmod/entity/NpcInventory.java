package com.npcmod.entity;

import net.minecraft.inventory.SimpleInventory;

public class NpcInventory extends SimpleInventory {

    public static final int SIZE = 6;
    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_FEET = 3;
    public static final int SLOT_MAINHAND = 4;
    public static final int SLOT_OFFHAND = 5;

    public NpcInventory() {
        super(SIZE);
    }
}