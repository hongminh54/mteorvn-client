/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.widgets;

import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.gui.tabs.Tab;
import meteordevelopment.meteorvnclient.gui.tabs.TabScreen;
import meteordevelopment.meteorvnclient.gui.tabs.Tabs;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorvnclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public abstract class WTopBar extends WHorizontalList {
    protected abstract Color getButtonColor(boolean pressed, boolean hovered);

    protected abstract Color getNameColor();

    public WTopBar() {
        spacing = 0;
    }

    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            add(new WTopBarButton(tab));
        }
    }

    protected class WTopBarButton extends WPressable {
        private final Tab tab;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme.textWidth(tab.name) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            Screen screen = mc.currentScreen;

            if (!(screen instanceof TabScreen) || ((TabScreen) screen).tab != tab) {
                double mouseX = mc.mouse.getX();
                double mouseY = mc.mouse.getY();

                tab.openScreen(theme);
                glfwSetCursorPos(mc.getWindow().getHandle(), mouseX, mouseY);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            Color color = getButtonColor(pressed || (mc.currentScreen instanceof TabScreen && ((TabScreen) mc.currentScreen).tab == tab), mouseOver);

            renderer.quad(x, y, width, height, color);
            renderer.text(tab.name, x + pad, y + pad, getNameColor(), false);
        }
    }
}
