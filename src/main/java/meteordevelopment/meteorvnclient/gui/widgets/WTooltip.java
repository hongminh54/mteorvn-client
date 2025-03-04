/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.widgets;

import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WContainer;

public abstract class WTooltip extends WContainer implements WRoot {
    private boolean valid;

    protected String text;

    public WTooltip(String text) {
        this.text = text;
    }

    @Override
    public void init() {
        add(theme.label(text)).pad(4);
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!valid) {
            calculateSize();
            calculateWidgetPositions();

            valid = true;
        }

        return super.render(renderer, mouseX, mouseY, delta);
    }
}
