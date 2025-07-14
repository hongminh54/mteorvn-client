/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.utils.Cell;
import meteordevelopment.meteorvnclient.gui.widgets.WLabel;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WView;
import meteordevelopment.meteorvnclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorvnclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.renderer.Fonts;
import meteordevelopment.meteorvnclient.renderer.text.FontFamily;
import meteordevelopment.meteorvnclient.renderer.text.FontInfo;
import meteordevelopment.meteorvnclient.settings.FontFaceSetting;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FontFaceSettingScreen extends WindowScreen {
    private final FontFaceSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public FontFaceSettingScreen(GuiTheme theme, FontFaceSetting setting) {
        super(theme, TranslationUtils.translate("gui.screen.select_font", "Select Font"));

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        window.view.hasScrollBar = false;

        enterAction = () -> {
            List<Cell<?>> row = table.getRow(0);
            if (row == null) return;

            WWidget widget = row.get(2).widget();
            if (widget instanceof WButton button) {
                button.action.run();
            }
        };

        WView view = add(theme.view()).expandX().widget();
        view.scrollOnlyWhenMouseOver = false;
        table = view.add(theme.table()).expandX().widget();

        initTable();
    }

    private void initTable() {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            String name = fontFamily.getName();

            WLabel item = theme.label(name);
            if (!filterText.isEmpty() && !StringUtils.containsIgnoreCase(name, filterText)) continue;
            table.add(item);

            WDropdown<FontInfo.Type> dropdown = table.add(theme.dropdown(FontInfo.Type.Regular)).right().widget();

            WButton select = table.add(theme.button(TranslationUtils.translate("gui.select", "Select"))).expandCellX().right().widget();
            select.action = () -> {
                setting.set(fontFamily.get(dropdown.get()));
                close();
            };

            table.row();
        }
    }
}
