/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.accounts;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.systems.accounts.MicrosoftLogin;
import meteordevelopment.meteorvnclient.systems.accounts.types.MicrosoftAccount;

public class AddMicrosoftAccountScreen extends AddAccountScreen {
    public AddMicrosoftAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, TranslationUtils.translate("gui.add_microsoft_account", "Add Microsoft Account"), parent);
    }

    @Override
    public void initWidgets() {
        MicrosoftLogin.getRefreshToken(refreshToken -> {

            if (refreshToken != null) {
                MicrosoftAccount account = new MicrosoftAccount(refreshToken);
                AccountsScreen.addAccount(null, parent, account);
            }

            close();
        });

        add(theme.label(TranslationUtils.translate("gui.select_account_browser", "Please select the account to log into in your browser.")));

        WButton cancel = add(theme.button(TranslationUtils.translate("gui.cancel", "Cancel"))).expandX().widget();
        cancel.action = () -> {
            MicrosoftLogin.stopServer();
            close();
        };
    }

    @Override
    public void tick() {}

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
