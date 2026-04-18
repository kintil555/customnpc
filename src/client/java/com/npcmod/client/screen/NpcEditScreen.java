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

import java.util.ArrayList;
import java.util.List;

public class NpcEditScreen extends HandledScreen<NpcEditScreenHandler> {

    private static final int BG_COLOR       = 0xFF2B2B2B;
    private static final int PANEL_COLOR    = 0xFF3A3A3A;
    private static final int BORDER_COLOR   = 0xFF555555;
    private static final int LABEL_COLOR    = 0xFFCCCCCC;
    private static final int TITLE_COLOR    = 0xFFFFFFFF;
    private static final int SECTION_COLOR  = 0xFFAAAAAA;
    private static final int FIELD_BG       = 0xFF1A1A1A;

    private static final String[] PART_LABELS    = {"Head", "Body", "R.Arm", "L.Arm", "R.Leg", "L.Leg"};
    private static final String[] PART_KEYS_BASE = {
            "head", "body", "right_arm", "left_arm", "right_leg", "left_leg"
    };
    private static final boolean[] HAS_ROLL = {true, false, true, true, true, true};

    private int npcEntityId = -1;

    private final TextFieldWidget[][] rotFields = new TextFieldWidget[6][3];
    private TextFieldWidget skinField;
    private ButtonWidget applyRotBtn;
    private ButtonWidget applySkinBtn;

    private static final String[] AXIS = {"X°", "Y°", "Z°"};

    public NpcEditScreen(NpcEditScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = NpcEditScreenHandler.BACKGROUND_WIDTH;
        this.backgroundHeight = NpcEditScreenHandler.BACKGROUND_HEIGHT;
        this.npcEntityId = NpcModClient.lastOpenedNpcId;
    }

    @Override
    protected void init() {
        super.init();

        int rotX = this.x + 36;
        int rotStartY = this.y + 16;
        int rowH = 18;
        int fieldW = 42;
        int fieldH = 14;

        for (int part = 0; part < 6; part++) {
            int py = rotStartY + part * rowH;
            for (int axis = 0; axis < 3; axis++) {
                if (axis == 1 || HAS_ROLL[part]) {
                    TextFieldWidget field = new TextFieldWidget(
                            this.textRenderer,
                            rotX + axis * (fieldW + 2), py,
                            fieldW, fieldH,
                            Text.empty()
                    );
                    field.setMaxLength(8);
                    field.setText("0.0");
                    rotFields[part][axis] = field;
                    this.addDrawableChild(field);
                }
            }
        }

        int skinY = this.y + 130;
        skinField = new TextFieldWidget(
                this.textRenderer,
                this.x + 36, skinY,
                160, 14,
                Text.translatable("screen.npcmod.skin")
        );
        skinField.setMaxLength(128);
        skinField.setPlaceholder(Text.literal("Username or URL..."));
        this.addDrawableChild(skinField);

        applySkinBtn = ButtonWidget.builder(
                Text.translatable("screen.npcmod.apply"),
                b -> sendSkinUpdate()
        ).dimensions(this.x + 200, skinY - 1, 40, 16).build();
        this.addDrawableChild(applySkinBtn);

        applyRotBtn = ButtonWidget.builder(
                Text.literal("Apply"),
                b -> sendRotationUpdate()
        ).dimensions(this.x + 36, this.y + 148, 60, 14).build();
        this.addDrawableChild(applyRotBtn);

        populateFromSyncData();
    }

    private void populateFromSyncData() {
        NbtCompound data = NpcModClient.lastOpenedNpcData;
        if (data == null) return;

        setFieldDeg(rotFields[0][0], data, "head_pitch");
        setFieldDeg(rotFields[0][1], data, "head_yaw");
        setFieldDeg(rotFields[0][2], data, "head_roll");
        setFieldDeg(rotFields[1][1], data, "body_yaw");
        setFieldDeg(rotFields[2][0], data, "right_arm_pitch");
        setFieldDeg(rotFields[2][1], data, "right_arm_yaw");
        setFieldDeg(rotFields[2][2], data, "right_arm_roll");
        setFieldDeg(rotFields[3][0], data, "left_arm_pitch");
        setFieldDeg(rotFields[3][1], data, "left_arm_yaw");
        setFieldDeg(rotFields[3][2], data, "left_arm_roll");
        setFieldDeg(rotFields[4][0], data, "right_leg_pitch");
        setFieldDeg(rotFields[4][1], data, "right_leg_yaw");
        setFieldDeg(rotFields[4][2], data, "right_leg_roll");
        setFieldDeg(rotFields[5][0], data, "left_leg_pitch");
        setFieldDeg(rotFields[5][1], data, "left_leg_yaw");
        setFieldDeg(rotFields[5][2], data, "left_leg_roll");

        String skin = data.getString("skin_name");
        if (skin != null && !skin.isEmpty()) {
            skinField.setText(skin);
        }
    }

    private void setFieldDeg(TextFieldWidget field, NbtCompound nbt, String key) {
        if (field == null) return;
        float rad = nbt.getFloat(key);
        float deg = (float) Math.toDegrees(rad);
        field.setText(String.format("%.1f", deg));
    }

    private void sendRotationUpdate() {
        if (npcEntityId < 0) return;
        NbtCompound rot = new NbtCompound();
        putRadians(rot, "head_pitch",      rotFields[0][0]);
        putRadians(rot, "head_yaw",        rotFields[0][1]);
        putRadians(rot, "head_roll",       rotFields[0][2]);
        putRadians(rot, "body_yaw",        rotFields[1][1]);
        putRadians(rot, "right_arm_pitch", rotFields[2][0]);
        putRadians(rot, "right_arm_yaw",   rotFields[2][1]);
        putRadians(rot, "right_arm_roll",  rotFields[2][2]);
        putRadians(rot, "left_arm_pitch",  rotFields[3][0]);
        putRadians(rot, "left_arm_yaw",    rotFields[3][1]);
        putRadians(rot, "left_arm_roll",   rotFields[3][2]);
        putRadians(rot, "right_leg_pitch", rotFields[4][0]);
        putRadians(rot, "right_leg_yaw",   rotFields[4][1]);
        putRadians(rot, "right_leg_roll",  rotFields[4][2]);
        putRadians(rot, "left_leg_pitch",  rotFields[5][0]);
        putRadians(rot, "left_leg_yaw",    rotFields[5][1]);
        putRadians(rot, "left_leg_roll",   rotFields[5][2]);
        ClientPlayNetworking.send(new UpdateNpcRotationC2SPayload(npcEntityId, rot));
    }

    private void sendSkinUpdate() {
        if (npcEntityId < 0) return;
        String skin = skinField.getText().trim();
        ClientPlayNetworking.send(new UpdateNpcSkinC2SPayload(npcEntityId, skin));
        if (!skin.isEmpty()) {
            NpcSkinManager.getSkin(skin);
        }
    }

    private void putRadians(NbtCompound nbt, String key, TextFieldWidget field) {
        if (field == null) {
            nbt.putFloat(key, 0.0f);
            return;
        }
        try {
            float deg = Float.parseFloat(field.getText());
            nbt.putFloat(key, (float) Math.toRadians(deg));
        } catch (NumberFormatException e) {
            nbt.putFloat(key, 0.0f);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int bg = this.x;
        int top = this.y;
        int w = this.backgroundWidth;
        int h = this.backgroundHeight;

        context.fill(bg, top, bg + w, top + h, BG_COLOR);
        context.drawBorder(bg, top, w, h, BORDER_COLOR);

        context.fill(bg + 2, top + 2, bg + 30, top + h - 2, PANEL_COLOR);

        int rotPanelX = bg + 32;
        int rotPanelW = 160;
        context.fill(rotPanelX, top + 2, rotPanelX + rotPanelW, top + 128, PANEL_COLOR);

        context.fill(bg + 32, top + 128, bg + 248, top + 150, PANEL_COLOR);

        String[] slotLabels = {"HEAD", "CHEST", "LEGS", "FEET", "MAIN", "OFF"};
        for (int i = 0; i < 6; i++) {
            int sy = top + 20 + i * 18;
            context.drawBorder(bg + 5, sy - 1, 20, 18, BORDER_COLOR);
            context.drawText(textRenderer, slotLabels[i].substring(0, 1),
                    bg + 26, sy + 4, SECTION_COLOR, false);
        }

        int rotX = this.x + 36;
        int rotStartY = this.y + 16;
        int rowH = 18;

        context.drawText(textRenderer,
                Text.translatable("screen.npcmod.rotations").getString(),
                rotX, top + 5, TITLE_COLOR, true);

        String[] axisHdr = {"X°", "Y°", "Z°"};
        for (int a = 0; a < 3; a++) {
            context.drawText(textRenderer, axisHdr[a],
                    rotX + a * 44 + 16, top + 5, SECTION_COLOR, false);
        }

        for (int part = 0; part < 6; part++) {
            int py = rotStartY + part * rowH;
            context.drawText(textRenderer, PART_LABELS[part],
                    rotX - 30, py + 3, LABEL_COLOR, false);
        }

        context.drawText(textRenderer,
                Text.translatable("screen.npcmod.skin").getString(),
                bg + 36, top + 121, LABEL_COLOR, false);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer,
                Text.translatable("screen.npcmod.npc_edit").getString(),
                this.titleX, this.titleY, TITLE_COLOR, false);
        context.drawText(textRenderer,
                this.playerInventoryTitle.getString(),
                this.playerInventoryTitleX, this.playerInventoryTitleY, TITLE_COLOR, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public void close() {
        sendRotationUpdate();
        sendSkinUpdate();
        super.close();
    }
}