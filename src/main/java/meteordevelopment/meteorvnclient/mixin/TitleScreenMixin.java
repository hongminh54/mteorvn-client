/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixin;

import meteordevelopment.meteorvnclient.systems.config.Config;
import meteordevelopment.meteorvnclient.utils.player.TitleScreenCredits;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.get().titleScreenCredits.get()) TitleScreenCredits.render(context);

        // Render custom button styling
        renderButtonStyling(context, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (Config.get().titleScreenCredits.get() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (TitleScreenCredits.onClicked(mouseX, mouseY)) info.setReturnValue(true);
        }
    }

    private void renderButtonStyling(DrawContext context, int mouseX, int mouseY) {
        // Duyệt qua tất cả children để tìm ButtonWidget
        for (Element element : this.children()) {
            if (element instanceof ButtonWidget button) {
                renderCustomButtonOverlay(context, button, mouseX, mouseY);
            }
        }
    }

    private void renderCustomButtonOverlay(DrawContext context, ButtonWidget button, int mouseX, int mouseY) {
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();

        // Kiểm tra hover
        boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        // Vẽ background overlay cho button
        if (hovered) {
            // Background hover với gradient
            context.fillGradient(x, y, x + width, y + height,
                0x40FFFFFF, 0x20FFFFFF); // Trắng nhẹ gradient
        } else {
            // Background bình thường với gradient nhẹ
            context.fillGradient(x, y, x + width, y + height,
                0x20000000, 0x40000000); // Đen nhẹ gradient
        }

        // Vẽ viền đẹp với rounded corners effect
        renderRoundedBorder(context, x, y, width, height, hovered);

        // Hiệu ứng glow khi hover
        if (hovered) {
            renderGlowEffect(context, x, y, width, height);
        }
    }

    private void renderRoundedBorder(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        // Màu viền dựa trên trạng thái hover
        int borderColor = hovered ? 0xFF00D4FF : 0xFF555555; // Cyan sáng khi hover, xám đậm bình thường
        int innerBorderColor = hovered ? 0xFF80E6FF : 0xFF777777; // Cyan nhạt inner, xám nhạt inner

        // Viền ngoài (main border)
        // Top
        context.fill(x, y, x + width, y + 1, borderColor);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        // Left
        context.fill(x, y, x + 1, y + height, borderColor);
        // Right
        context.fill(x + width - 1, y, x + width, y + height, borderColor);

        // Viền trong (inner border) để tạo depth
        if (hovered) {
            // Top inner
            context.fill(x + 1, y + 1, x + width - 1, y + 2, innerBorderColor);
            // Bottom inner
            context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, innerBorderColor);
            // Left inner
            context.fill(x + 1, y + 1, x + 2, y + height - 1, innerBorderColor);
            // Right inner
            context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, innerBorderColor);
        }

        // Corner highlights để tạo rounded effect
        if (hovered) {
            int cornerColor = 0xFF40A0FF;
            // Top-left corner
            context.fill(x, y, x + 2, y + 2, cornerColor);
            // Top-right corner
            context.fill(x + width - 2, y, x + width, y + 2, cornerColor);
            // Bottom-left corner
            context.fill(x, y + height - 2, x + 2, y + height, cornerColor);
            // Bottom-right corner
            context.fill(x + width - 2, y + height - 2, x + width, y + height, cornerColor);
        }
    }

    private void renderGlowEffect(DrawContext context, int x, int y, int width, int height) {
        // Outer glow với nhiều layer để tạo hiệu ứng mượt
        int[] glowColors = {
            0x60007FFF, // Layer ngoài cùng - xanh đậm
            0x80009FFF, // Layer giữa - xanh vừa
            0xA000BFFF, // Layer trong - xanh sáng
            0xC000DFFF  // Layer trong cùng - xanh rất sáng
        };

        for (int i = 0; i < glowColors.length; i++) {
            int glowSize = glowColors.length - i;
            int color = glowColors[i];

            // Top glow
            context.fill(x - glowSize, y - glowSize, x + width + glowSize, y, color);
            // Bottom glow
            context.fill(x - glowSize, y + height, x + width + glowSize, y + height + glowSize, color);
            // Left glow
            context.fill(x - glowSize, y - glowSize, x, y + height + glowSize, color);
            // Right glow
            context.fill(x + width, y - glowSize, x + width + glowSize, y + height + glowSize, color);
        }

        // Pulse effect - thêm hiệu ứng nhấp nháy nhẹ
        long time = System.currentTimeMillis();
        float pulse = (float) (0.5 + 0.3 * Math.sin(time * 0.005)); // Pulse từ 0.2 đến 0.8
        int pulseColor = (int) (0x40 * pulse) << 24 | 0x00FFFF; // Alpha thay đổi theo pulse

        // Pulse border
        context.fill(x - 1, y - 1, x + width + 1, y, pulseColor);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, pulseColor);
        context.fill(x - 1, y, x, y + height, pulseColor);
        context.fill(x + width, y, x + width + 1, y + height, pulseColor);
    }
}
