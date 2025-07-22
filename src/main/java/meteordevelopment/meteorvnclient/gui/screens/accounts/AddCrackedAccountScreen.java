/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.accounts;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorvnclient.systems.accounts.Accounts;
import meteordevelopment.meteorvnclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;

public class AddCrackedAccountScreen extends AddAccountScreen {
    public AddCrackedAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, TranslationUtils.translate("gui.add_cracked_account", "Add Cracked Account"), parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Name
        t.add(theme.label(TranslationUtils.translate("gui.name", "Name: ")));
        WTextBox name = t.add(theme.textBox("", "seasnail8169", (text, c) ->
            // Username can't contain spaces
            c != ' '
        )).minWidth(400).expandX().widget();
        name.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button(TranslationUtils.translate("gui.add", "Add"))).expandX().widget();
        add.action = () -> {
            if (!name.get().isEmpty() && name.get().length() < 17) {
                CrackedAccount account = new CrackedAccount(name.get());
                if (!(Accounts.get().exists(account))) {
                    AccountsScreen.addAccount(this, parent, account);
                }
            }
        };

        enterAction = add.action;
    }
}
