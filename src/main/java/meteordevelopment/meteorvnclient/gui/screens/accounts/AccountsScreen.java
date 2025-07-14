/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.accounts;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.widgets.WAccount;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.systems.accounts.Account;
import meteordevelopment.meteorvnclient.systems.accounts.Accounts;
import meteordevelopment.meteorvnclient.utils.misc.NbtUtils;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import org.jetbrains.annotations.Nullable;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class AccountsScreen extends WindowScreen {
    public AccountsScreen(GuiTheme theme) {
        super(theme, TranslationUtils.translate("gui.accounts", "Accounts"));
    }

    @Override
    public void initWidgets() {
        // Accounts
        for (Account<?> account : Accounts.get()) {
            WAccount wAccount = add(theme.account(this, account)).expandX().widget();
            wAccount.refreshScreenAction = this::reload;
        }

        // Add account
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        addButton(l, TranslationUtils.translate("gui.cracked", "Cracked"), () -> mc.setScreen(new AddCrackedAccountScreen(theme, this)));
        addButton(l, TranslationUtils.translate("gui.altening", "Altening"), () -> mc.setScreen(new AddAlteningAccountScreen(theme, this)));
        addButton(l, TranslationUtils.translate("gui.microsoft", "Microsoft"), () -> mc.setScreen(new AddMicrosoftAccountScreen(theme, this)));
    }

    private void addButton(WContainer c, String text, Runnable action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = action;
    }

    public static void addAccount(@Nullable AddAccountScreen screen, AccountsScreen parent, Account<?> account) {
        if (screen != null) screen.locked = true;

        MeteorExecutor.execute(() -> {
            if (account.fetchInfo()) {
                account.getCache().loadHead();

                Accounts.get().add(account);
                if (account.login()) Accounts.get().save();

                if (screen != null) {
                    screen.locked = false;
                    screen.close();
                }

                parent.reload();

                return;
            }

            if (screen != null) screen.locked = false;
        });
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Accounts.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Accounts.get());
    }
}
