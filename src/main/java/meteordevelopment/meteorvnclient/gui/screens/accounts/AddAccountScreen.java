/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.accounts;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;

public abstract class AddAccountScreen extends WindowScreen {
    public final AccountsScreen parent;
    public WButton add;
    private int timer;

    protected AddAccountScreen(GuiTheme theme, String title, AccountsScreen parent) {
        super(theme, title);
        this.parent = parent;
    }

    @Override
    public void tick() {
        if (locked) {
            if (timer > 2) {
                add.set(getNext(add));
                timer = 0;
            }
            else {
                timer++;
            }
        }

        else if (!add.getText().equals(TranslationUtils.translate("gui.add", "Add"))) {
            add.set(TranslationUtils.translate("gui.add", "Add"));
        }
    }

    private String getNext(WButton add) {
        String addText = TranslationUtils.translate("gui.add", "Add");
        return switch (add.getText()) {
            case "Add", "oo0" -> "ooo";
            case "ooo" -> "0oo";
            case "0oo" -> "o0o";
            case "o0o" -> "oo0";
            default -> addText;
        };
    }
}
