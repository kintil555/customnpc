package com.npcmod.entity;

import com.npcmod.ModScreenHandlers;
import com.npcmod.NpcMod;
import com.npcmod.network.SyncNpcDataS2CPayload;
import com.npcmod.screen.NpcEditScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class NpcEntity extends LivingEntity implements NamedScreenHandlerFactory {

    private static final TrackedData<Float> NPC_HEAD_PITCH =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_HEAD_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_HEAD_ROLL =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_BODY_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_ARM_PITCH =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_ARM_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_ARM_ROLL =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_ARM_PITCH =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_ARM_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_ARM_ROLL =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_LEG_PITCH =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_LEG_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_RIGHT_LEG_ROLL =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_LEG_PITCH =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_LEG_YAW =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> NPC_LEFT_LEG_ROLL =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<String> NPC_SKIN_NAME =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.STRING);

    private final NpcInventory inventory = new NpcInventory();

    public NpcEntity(EntityType<? extends NpcEntity> type, World world) {
        super(type, world);
        this.setInvulnerable(true);
        this.setNoGravity(false);
        this.noClip = false;
    }

    public static DefaultAttributeContainer.Builder createNpcAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(NPC_HEAD_PITCH, 0.0f);
        builder.add(NPC_HEAD_YAW, 0.0f);
        builder.add(NPC_HEAD_ROLL, 0.0f);
        builder.add(NPC_BODY_YAW, 0.0f);
        builder.add(NPC_RIGHT_ARM_PITCH, 0.0f);
        builder.add(NPC_RIGHT_ARM_YAW, 0.0f);
        builder.add(NPC_RIGHT_ARM_ROLL, 0.0f);
        builder.add(NPC_LEFT_ARM_PITCH, 0.0f);
        builder.add(NPC_LEFT_ARM_YAW, 0.0f);
        builder.add(NPC_LEFT_ARM_ROLL, 0.0f);
        builder.add(NPC_RIGHT_LEG_PITCH, 0.0f);
        builder.add(NPC_RIGHT_LEG_YAW, 0.0f);
        builder.add(NPC_RIGHT_LEG_ROLL, 0.0f);
        builder.add(NPC_LEFT_LEG_PITCH, 0.0f);
        builder.add(NPC_LEFT_LEG_YAW, 0.0f);
        builder.add(NPC_LEFT_LEG_ROLL, 0.0f);
        builder.add(NPC_SKIN_NAME, "");
    }

    // Solid collision - tidak tembus blok/entity lain
    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void pushAwayFrom(net.minecraft.entity.Entity entity) {
        // NPC tidak terdorong
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            // Sneak + klik = pickup jadi item kembali ke inventory
            if (player.isSneaking()) {
                ItemStack drop = new ItemStack(com.npcmod.ModItems.NPC_SPAWNER);
                // Simpan data NPC ke NBT item
                NbtCompound npcData = new NbtCompound();
                npcData.put("NpcData", buildSyncNbt());
                drop.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                        net.minecraft.component.type.NbtComponent.of(npcData));
                player.getInventory().offerOrDrop(drop);
                this.discard();
                return ActionResult.SUCCESS;
            }

            // Klik biasa = buka GUI
            NbtCompound syncData = buildSyncNbt();
            ServerPlayNetworking.send(serverPlayer,
                    new SyncNpcDataS2CPayload(this.getId(), syncData));
            NpcMod.PENDING_GUI_ENTITY.put(player.getUuid(), this.getId());
            player.openHandledScreen(this);
        }
        return ActionResult.SUCCESS;
    }

    private NbtCompound buildSyncNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("head_pitch", this.dataTracker.get(NPC_HEAD_PITCH));
        nbt.putFloat("head_yaw", this.dataTracker.get(NPC_HEAD_YAW));
        nbt.putFloat("head_roll", this.dataTracker.get(NPC_HEAD_ROLL));
        nbt.putFloat("body_yaw", this.dataTracker.get(NPC_BODY_YAW));
        nbt.putFloat("right_arm_pitch", this.dataTracker.get(NPC_RIGHT_ARM_PITCH));
        nbt.putFloat("right_arm_yaw", this.dataTracker.get(NPC_RIGHT_ARM_YAW));
        nbt.putFloat("right_arm_roll", this.dataTracker.get(NPC_RIGHT_ARM_ROLL));
        nbt.putFloat("left_arm_pitch", this.dataTracker.get(NPC_LEFT_ARM_PITCH));
        nbt.putFloat("left_arm_yaw", this.dataTracker.get(NPC_LEFT_ARM_YAW));
        nbt.putFloat("left_arm_roll", this.dataTracker.get(NPC_LEFT_ARM_ROLL));
        nbt.putFloat("right_leg_pitch", this.dataTracker.get(NPC_RIGHT_LEG_PITCH));
        nbt.putFloat("right_leg_yaw", this.dataTracker.get(NPC_RIGHT_LEG_YAW));
        nbt.putFloat("right_leg_roll", this.dataTracker.get(NPC_RIGHT_LEG_ROLL));
        nbt.putFloat("left_leg_pitch", this.dataTracker.get(NPC_LEFT_LEG_PITCH));
        nbt.putFloat("left_leg_yaw", this.dataTracker.get(NPC_LEFT_LEG_YAW));
        nbt.putFloat("left_leg_roll", this.dataTracker.get(NPC_LEFT_LEG_ROLL));
        nbt.putString("skin_name", this.dataTracker.get(NPC_SKIN_NAME));
        return nbt;
    }

    public void applyRotationsFromNbt(NbtCompound nbt) {
        this.dataTracker.set(NPC_HEAD_PITCH, nbt.getFloat("head_pitch"));
        this.dataTracker.set(NPC_HEAD_YAW, nbt.getFloat("head_yaw"));
        this.dataTracker.set(NPC_HEAD_ROLL, nbt.getFloat("head_roll"));
        this.dataTracker.set(NPC_BODY_YAW, nbt.getFloat("body_yaw"));
        this.dataTracker.set(NPC_RIGHT_ARM_PITCH, nbt.getFloat("right_arm_pitch"));
        this.dataTracker.set(NPC_RIGHT_ARM_YAW, nbt.getFloat("right_arm_yaw"));
        this.dataTracker.set(NPC_RIGHT_ARM_ROLL, nbt.getFloat("right_arm_roll"));
        this.dataTracker.set(NPC_LEFT_ARM_PITCH, nbt.getFloat("left_arm_pitch"));
        this.dataTracker.set(NPC_LEFT_ARM_YAW, nbt.getFloat("left_arm_yaw"));
        this.dataTracker.set(NPC_LEFT_ARM_ROLL, nbt.getFloat("left_arm_roll"));
        this.dataTracker.set(NPC_RIGHT_LEG_PITCH, nbt.getFloat("right_leg_pitch"));
        this.dataTracker.set(NPC_RIGHT_LEG_YAW, nbt.getFloat("right_leg_yaw"));
        this.dataTracker.set(NPC_RIGHT_LEG_ROLL, nbt.getFloat("right_leg_roll"));
        this.dataTracker.set(NPC_LEFT_LEG_PITCH, nbt.getFloat("left_leg_pitch"));
        this.dataTracker.set(NPC_LEFT_LEG_YAW, nbt.getFloat("left_leg_yaw"));
        this.dataTracker.set(NPC_LEFT_LEG_ROLL, nbt.getFloat("left_leg_roll"));
    }

    public void setSkinName(String name) {
        this.dataTracker.set(NPC_SKIN_NAME, name == null ? "" : name);
    }

    public String getSkinName() { return this.dataTracker.get(NPC_SKIN_NAME); }

    public float getNpcHeadPitch()     { return this.dataTracker.get(NPC_HEAD_PITCH); }
    public float getNpcHeadYaw()       { return this.dataTracker.get(NPC_HEAD_YAW); }
    public float getNpcHeadRoll()      { return this.dataTracker.get(NPC_HEAD_ROLL); }
    public float getNpcBodyYaw()       { return this.dataTracker.get(NPC_BODY_YAW); }
    public float getNpcRightArmPitch() { return this.dataTracker.get(NPC_RIGHT_ARM_PITCH); }
    public float getNpcRightArmYaw()   { return this.dataTracker.get(NPC_RIGHT_ARM_YAW); }
    public float getNpcRightArmRoll()  { return this.dataTracker.get(NPC_RIGHT_ARM_ROLL); }
    public float getNpcLeftArmPitch()  { return this.dataTracker.get(NPC_LEFT_ARM_PITCH); }
    public float getNpcLeftArmYaw()    { return this.dataTracker.get(NPC_LEFT_ARM_YAW); }
    public float getNpcLeftArmRoll()   { return this.dataTracker.get(NPC_LEFT_ARM_ROLL); }
    public float getNpcRightLegPitch() { return this.dataTracker.get(NPC_RIGHT_LEG_PITCH); }
    public float getNpcRightLegYaw()   { return this.dataTracker.get(NPC_RIGHT_LEG_YAW); }
    public float getNpcRightLegRoll()  { return this.dataTracker.get(NPC_RIGHT_LEG_ROLL); }
    public float getNpcLeftLegPitch()  { return this.dataTracker.get(NPC_LEFT_LEG_PITCH); }
    public float getNpcLeftLegYaw()    { return this.dataTracker.get(NPC_LEFT_LEG_YAW); }
    public float getNpcLeftLegRoll()   { return this.dataTracker.get(NPC_LEFT_LEG_ROLL); }

    public NpcInventory getNpcInventory() { return inventory; }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of(
                inventory.getStack(NpcInventory.SLOT_FEET),
                inventory.getStack(NpcInventory.SLOT_LEGS),
                inventory.getStack(NpcInventory.SLOT_CHEST),
                inventory.getStack(NpcInventory.SLOT_HEAD)
        );
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD     -> inventory.getStack(NpcInventory.SLOT_HEAD);
            case CHEST    -> inventory.getStack(NpcInventory.SLOT_CHEST);
            case LEGS     -> inventory.getStack(NpcInventory.SLOT_LEGS);
            case FEET     -> inventory.getStack(NpcInventory.SLOT_FEET);
            case MAINHAND -> inventory.getStack(NpcInventory.SLOT_MAINHAND);
            case OFFHAND  -> inventory.getStack(NpcInventory.SLOT_OFFHAND);
            default       -> ItemStack.EMPTY;
        };
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        int idx = switch (slot) {
            case HEAD     -> NpcInventory.SLOT_HEAD;
            case CHEST    -> NpcInventory.SLOT_CHEST;
            case LEGS     -> NpcInventory.SLOT_LEGS;
            case FEET     -> NpcInventory.SLOT_FEET;
            case MAINHAND -> NpcInventory.SLOT_MAINHAND;
            case OFFHAND  -> NpcInventory.SLOT_OFFHAND;
            default       -> -1;
        };
        if (idx >= 0) inventory.setStack(idx, stack);
    }

    @Override
    public Arm getMainArm() { return Arm.RIGHT; }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.npcmod.npc");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NpcEditScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("NpcRotations", buildSyncNbt());
        RegistryWrapper.WrapperLookup registries = this.getWorld().getRegistryManager();
        NbtList inventoryNbt = new NbtList();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound slotNbt = new NbtCompound();
                slotNbt.putByte("Slot", (byte) i);
                slotNbt.put("Item", stack.toNbt(registries));
                inventoryNbt.add(slotNbt);
            }
        }
        nbt.put("NpcInventory", inventoryNbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("NpcRotations")) {
            applyRotationsFromNbt(nbt.getCompound("NpcRotations"));
            String skin = nbt.getCompound("NpcRotations").getString("skin_name");
            this.setSkinName(skin);
        }
        if (nbt.contains("NpcInventory")) {
            RegistryWrapper.WrapperLookup registries = this.getWorld().getRegistryManager();
            NbtList inventoryNbt = nbt.getList("NpcInventory", 10);
            for (int i = 0; i < inventoryNbt.size(); i++) {
                NbtCompound slotNbt = inventoryNbt.getCompound(i);
                int slot = slotNbt.getByte("Slot") & 255;
                if (slot < inventory.size()) {
                    ItemStack.fromNbt(registries, slotNbt.getCompound("Item"))
                            .ifPresent(stack -> inventory.setStack(slot, stack));
                }
            }
        }
    }
}
