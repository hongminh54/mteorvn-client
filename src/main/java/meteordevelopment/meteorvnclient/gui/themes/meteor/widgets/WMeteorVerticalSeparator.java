/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorvnclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorvnclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorvnclient.utils.render.color.Color;

public class WMeteorVerticalSeparator extends WVerticalSeparator implements MeteorWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        Color colorEdges = theme.separatorEdges.get();
        Color colorCenter = theme.separatorCenter.get();

        double s = theme.scale(1);
        double offsetX = Math.round(width / 2.0);

        renderer.quad(x + offsetX, y, s, height / 2, colorEdges, colorEdges, colorCenter, colorCenter);
        renderer.quad(x + offsetX, y + height / 2, s, height / 2, colorCenter, colorCenter, colorEdges, colorEdges);
    }
}
