/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.accounts;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.systems.accounts.Account;
import meteordevelopment.meteorvnclient.systems.accounts.TokenAccount;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.render.color.Color;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class AccountInfoScreen extends WindowScreen {
    private final Account<?> account;

    public AccountInfoScreen(GuiTheme theme, Account<?> account) {
        super(theme, account.getUsername() + " details");
        this.account = account;
    }

    @Override
    public void initWidgets() {
        TokenAccount e = (TokenAccount) account;
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        WButton copy = theme.button(TranslationUtils.translate("gui.copy", "Copy"));
        copy.action = () -> mc.keyboard.setClipboard(e.getToken());

        l.add(theme.label(TranslationUtils.translate("gui.altening_token", "TheAltening token:")));
        l.add(theme.label(e.getToken()).color(Color.GRAY)).pad(5);
        l.add(copy);
    }
}
