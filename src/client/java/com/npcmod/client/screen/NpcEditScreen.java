package com.npcmod.client.screen;

import com.npcmod.NpcModClient;
import com.npcmod.client.entity.NpcSkinManager;
import com.npcmod.network.UpdateNpcRotationC2SPayload;
import com.npcmod.network.UpdateNpcSkinC2SPayload;
import com.npcmod.screen.NpcEditScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class NpcEditScreen extends HandledScreen<NpcEditScreenHandler> {

    // Warna
    private static final int COL_BG      = 0xFF1E1E2E;
    private static final int COL_PANEL   = 0xFF2A2A3E;
    private static final int COL_BORDER  = 0xFF5555AA;
    private static final int COL_HEADER  = 0xFF7777FF;
    private static final int COL_LABEL   = 0xFFCCCCDD;
    private static final int COL_WHITE   = 0xFFFFFFFF;
    private static final int COL_AXIS    = 0xFF88AAFF;

    // Layout — panel kiri (slot) dan panel kanan (rotasi + skin)
    private static final int LEFT_W      = 36;   // lebar panel slot
    private static final int RIGHT_X     = 38;   // mulai panel rotasi
    private static final int FIELD_W     = 40;
    private static final int FIELD_H     = 12;
    private static final int ROW_H       = 16;
    private static final int HEADER_H    = 14;

    private static final String[] PART_LABELS = {"Head", "Body", "R.Arm", "L.Arm", "R.Leg", "L.Leg"};
    // axis: 0=X(pitch), 1=Y(yaw), 2=Z(roll). Body hanya punya Y.
    private static final boolean[] HAS_X = {true,  false, true,  true,  true,  true};
    private static final boolean[] HAS_Z = {true,  false, true,  true,  true,  true};

    private int npcEntityId = -1;
    private final TextFieldWidget[][] rotFields = new TextFieldWidget[6][3]; // [part][0=X,1=Y,2=Z]
    private TextFieldWidget skinField;
    private ButtonWidget applyRotBtn;
    private ButtonWidget applySkinBtn;

    public NpcEditScreen(NpcEditScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = NpcEditScreenHandler.BACKGROUND_WIDTH;
        this.backgroundHeight = NpcEditScreenHandler.BACKGROUND_HEIGHT;
        this.npcEntityId = NpcModClient.lastOpenedNpcId;
    }

    @Override
    protected void init() {
        super.init();

        // Posisi absolut area rotasi
        int rx = this.x + RIGHT_X + 38; // geser kanan setelah label part
        int ry = this.y + HEADER_H + 4;

        for (int part = 0; part < 6; part++) {
            int py = ry + part * ROW_H;
            for (int axis = 0; axis < 3; axis++) {
                boolean enabled = axis == 1                          // Y selalu ada
                        || (axis == 0 && HAS_X[part])
                        || (axis == 2 && HAS_Z[part]);
                if (!enabled) continue;

                TextFieldWidget field = new TextFieldWidget(
                        this.textRenderer,
                        rx + axis * (FIELD_W + 3), py,
                        FIELD_W, FIELD_H,
                        Text.empty()
                );
                field.setMaxLength(8);
                field.setText("0.0");
                rotFields[part][axis] = field;
                this.addDrawableChild(field);
            }
        }

        // Baris skin — di bawah rotasi dengan jarak cukup
        int skinY = this.y + HEADER_H + 4 + 6 * ROW_H + 6;
        skinField = new TextFieldWidget(
                this.textRenderer,
                this.x + RIGHT_X, skinY,
                168, FIELD_H,
                Text.empty()
        );
        skinField.setMaxLength(128);
        skinField.setPlaceholder(Text.literal("Username or skin URL..."));
        this.addDrawableChild(skinField);

        applySkinBtn = ButtonWidget.builder(
                Text.literal("Apply"),
                b -> sendSkinUpdate()
        ).dimensions(this.x + RIGHT_X + 172, skinY - 1, 38, FIELD_H + 2).build();
        this.addDrawableChild(applySkinBtn);

        applyRotBtn = ButtonWidget.builder(
                Text.literal("Apply Rotations"),
                b -> sendRotationUpdate()
        ).dimensions(this.x + RIGHT_X, skinY + FIELD_H + 4, 100, 14).build();
        this.addDrawableChild(applyRotBtn);

        populateFromSyncData();
    }

    private void populateFromSyncData() {
        NbtCompound data = NpcModClient.lastOpenedNpcData;
        if (data == null) return;

        setDeg(rotFields[0][0], data, "head_pitch");
        setDeg(rotFields[0][1], data, "head_yaw");
        setDeg(rotFields[0][2], data, "head_roll");
        setDeg(rotFields[1][1], data, "body_yaw");
        setDeg(rotFields[2][0], data, "right_arm_pitch");
        setDeg(rotFields[2][1], data, "right_arm_yaw");
        setDeg(rotFields[2][2], data, "right_arm_roll");
        setDeg(rotFields[3][0], data, "left_arm_pitch");
        setDeg(rotFields[3][1], data, "left_arm_yaw");
        setDeg(rotFields[3][2], data, "left_arm_roll");
        setDeg(rotFields[4][0], data, "right_leg_pitch");
        setDeg(rotFields[4][1], data, "right_leg_yaw");
        setDeg(rotFields[4][2], data, "right_leg_roll");
        setDeg(rotFields[5][0], data, "left_leg_pitch");
        setDeg(rotFields[5][1], data, "left_leg_yaw");
        setDeg(rotFields[5][2], data, "left_leg_roll");

        String skin = data.getString("skin_name");
        if (skin != null && !skin.isEmpty()) skinField.setText(skin);
    }

    private void setDeg(TextFieldWidget f, NbtCompound nbt, String key) {
        if (f == null) return;
        f.setText(String.format("%.1f", (float) Math.toDegrees(nbt.getFloat(key))));
    }

    private void sendRotationUpdate() {
        if (npcEntityId < 0) return;
        NbtCompound rot = new NbtCompound();
        putRad(rot, "head_pitch",      rotFields[0][0]);
        putRad(rot, "head_yaw",        rotFields[0][1]);
        putRad(rot, "head_roll",       rotFields[0][2]);
        putRad(rot, "body_yaw",        rotFields[1][1]);
        putRad(rot, "right_arm_pitch", rotFields[2][0]);
        putRad(rot, "right_arm_yaw",   rotFields[2][1]);
        putRad(rot, "right_arm_roll",  rotFields[2][2]);
        putRad(rot, "left_arm_pitch",  rotFields[3][0]);
        putRad(rot, "left_arm_yaw",    rotFields[3][1]);
        putRad(rot, "left_arm_roll",   rotFields[3][2]);
        putRad(rot, "right_leg_pitch", rotFields[4][0]);
        putRad(rot, "right_leg_yaw",   rotFields[4][1]);
        putRad(rot, "right_leg_roll",  rotFields[4][2]);
        putRad(rot, "left_leg_pitch",  rotFields[5][0]);
        putRad(rot, "left_leg_yaw",    rotFields[5][1]);
        putRad(rot, "left_leg_roll",   rotFields[5][2]);
        ClientPlayNetworking.send(new UpdateNpcRotationC2SPayload(npcEntityId, rot));
    }

    private void sendSkinUpdate() {
        if (npcEntityId < 0) return;
        String skin = skinField.getText().trim();
        ClientPlayNetworking.send(new UpdateNpcSkinC2SPayload(npcEntityId, skin));
        if (!skin.isEmpty()) NpcSkinManager.getSkin(skin);
    }

    private void putRad(NbtCompound nbt, String key, TextFieldWidget f) {
        if (f == null) { nbt.putFloat(key, 0f); return; }
        try {
            nbt.putFloat(key, (float) Math.toRadians(Float.parseFloat(f.getText())));
        } catch (NumberFormatException e) {
            nbt.putFloat(key, 0f);
        }
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mx, int my) {
        int x = this.x, y = this.y, w = this.backgroundWidth, h = this.backgroundHeight;

        // Background utama
        ctx.fill(x, y, x + w, y + h, COL_BG);
        ctx.drawBorder(x, y, w, h, COL_BORDER);

        // Panel kiri — slot equipment
        ctx.fill(x + 1, y + 1, x + LEFT_W + 2, y + h - 1, COL_PANEL);
        ctx.drawBorder(x + 1, y + 1, LEFT_W + 1, h - 2, COL_BORDER);
        ctx.drawText(textRenderer, "Equip", x + 4, y + 4, COL_HEADER, true);

        // Divider vertikal
        ctx.fill(x + LEFT_W + 2, y + 1, x + LEFT_W + 3, y + h - 1, COL_BORDER);

        // Panel kanan — rotasi
        int rx = x + RIGHT_X;
        int ry = y + HEADER_H + 4;

        // Header rotasi
        ctx.fill(rx, y + 1, x + w - 1, y + HEADER_H + 2, COL_PANEL);
        ctx.drawText(textRenderer, "Edit Rotations", rx + 2, y + 4, COL_HEADER, true);

        // Header kolom axis
        String[] axisLabels = {"X°", "Y°", "Z°"};
        int labelX = rx + 40;
        for (int a = 0; a < 3; a++) {
            ctx.drawText(textRenderer, axisLabels[a],
                    labelX + a * (FIELD_W + 3) + FIELD_W / 2 - 4,
                    y + HEADER_H + 4 - 11, COL_AXIS, false);
        }

        // Label baris part + background strip selang-seling
        for (int part = 0; part < 6; part++) {
            int py = ry + part * ROW_H;
            if (part % 2 == 0)
                ctx.fill(rx, py - 1, x + w - 2, py + ROW_H - 3, 0x11FFFFFF);
            ctx.drawText(textRenderer, PART_LABELS[part], rx + 2, py + 2, COL_LABEL, false);
        }

        // Divider sebelum skin
        int skinDivY = ry + 6 * ROW_H + 2;
        ctx.fill(rx, skinDivY, x + w - 2, skinDivY + 1, COL_BORDER);
        ctx.drawText(textRenderer, "Skin (username / URL)", rx + 2, skinDivY + 3, COL_HEADER, false);

        // Panel player inventory
        int invY = y + h - 78;
        ctx.fill(x + 1, invY - 2, x + w - 1, y + h - 1, COL_PANEL);
        ctx.drawBorder(x + 1, invY - 2, w - 2, h - invY + 1, COL_BORDER);
        ctx.drawText(textRenderer, "Inventory", x + 5, invY - 10, COL_LABEL, false);
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mx, int my) {
        // Kosongkan — sudah digambar di drawBackground agar tidak double
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);
        this.drawMouseoverTooltip(ctx, mx, my);
    }

    @Override
    public void close() {
        sendRotationUpdate();
        sendSkinUpdate();
        super.close();
    }
}
