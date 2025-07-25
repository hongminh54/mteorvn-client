/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorvnclient.utils.render.color.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class MeteorToast implements Toast {
    public static final int TITLE_COLOR = Color.fromRGBA(145, 61, 226, 255);
    public static final int TEXT_COLOR = Color.fromRGBA(220, 220, 220, 255);
    private static final Identifier TEXTURE = Identifier.of("textures/gui/sprites/toast/advancement.png");

    private ItemStack icon;
    private Text title, text;
    private boolean justUpdated = true, playedSound;
    private long start, duration;
    private Toast.Visibility visibility = Visibility.HIDE;

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text, long duration) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.title = Text.literal(title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TITLE_COLOR)));
        this.text = text != null ? Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_COLOR))) : null;
        this.duration = duration;
    }

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.title = Text.literal(title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TITLE_COLOR)));
        this.text = text != null ? Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_COLOR))) : null;
        this.duration = 6000;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (justUpdated) {
            start = time;
            justUpdated = false;
        }

        visibility = time - start >= duration ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;

        if (!playedSound) {
            mc.getSoundManager().play(getSound());
            playedSound = true;
        }
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        RenderSystem.setShaderColor(1, 1, 1, 1);

        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, getWidth(), getHeight());

        int x = icon != null ? 28 : 12;
        int titleY = 12;

        if (text != null) {
            context.drawText(mc.textRenderer, title, x, 18, TITLE_COLOR, false);
            titleY = 7;
        }

        context.drawText(mc.textRenderer, title, x, titleY, TITLE_COLOR, false);

        if (icon != null) context.drawItem(icon, 8, 8);
    }

    public void setIcon(@Nullable Item item) {
        this.icon = item != null ? item.getDefaultStack() : null;
        justUpdated = true;
    }

    public void setTitle(@NotNull String title) {
        this.title = Text.literal(title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TITLE_COLOR)));
        justUpdated = true;
    }

    public void setText(@Nullable String text) {
        this.text = text != null ? Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_COLOR))) : null;
        justUpdated = true;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        justUpdated = true;
    }

    // Addons: @Override this method in a subclass if you want a different sound
    public SoundInstance getSound() {
        return PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.2f, 1);
    }
}
