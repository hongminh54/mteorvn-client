/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixin;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.events.meteor.CharTypedEvent;
import meteordevelopment.meteorvnclient.events.meteor.KeyEvent;
import meteordevelopment.meteorvnclient.gui.GuiKeyEvents;
import meteordevelopment.meteorvnclient.gui.WidgetScreen;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.input.Input;
import meteordevelopment.meteorvnclient.utils.misc.input.KeyAction;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            // on Linux/X11 the modifier is not active when the key is pressed and still active when the key is released
            // https://github.com/glfw/glfw/issues/1630
            if (action == GLFW.GLFW_PRESS) {
                modifiers |= Input.getModifier(key);
            } else if (action == GLFW.GLFW_RELEASE) {
                modifiers &= ~Input.getModifier(key);
            }

            if (client.currentScreen instanceof WidgetScreen && action == GLFW.GLFW_REPEAT) {
                ((WidgetScreen) client.currentScreen).keyRepeated(key, modifiers);
            }

            if (GuiKeyEvents.canUseKeys) {
                Input.setKeyState(key, action != GLFW.GLFW_RELEASE);
                if (MeteorVNClient.EVENT_BUS.post(KeyEvent.get(key, modifiers, KeyAction.get(action))).isCancelled()) info.cancel();
            }
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int i, int j, CallbackInfo info) {
        if (Utils.canUpdate() && !client.isPaused() && (client.currentScreen == null || client.currentScreen instanceof WidgetScreen)) {
            if (MeteorVNClient.EVENT_BUS.post(CharTypedEvent.get((char) i)).isCancelled()) info.cancel();
        }
    }
}
