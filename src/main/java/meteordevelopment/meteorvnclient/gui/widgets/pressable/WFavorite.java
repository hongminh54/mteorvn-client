/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.widgets.pressable;

import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.utils.render.color.Color;

public abstract class WFavorite extends WPressable {
    public boolean checked;

    public WFavorite(boolean checked) {
        this.checked = checked;
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();
        double s = theme.textHeight();

        width = pad + s + pad;
        height = pad + s + pad;
    }

    @Override
    protected void onPressed(int button) {
        checked = !checked;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = theme.textHeight();

        renderer.quad(x + pad, y + pad, s, s, checked ? GuiRenderer.FAVORITE_YES : GuiRenderer.FAVORITE_NO, getColor());
    }

    protected abstract Color getColor();
}
