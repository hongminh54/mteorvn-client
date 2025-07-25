/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.widgets;

import meteordevelopment.meteorvnclient.gui.WidgetScreen;
import meteordevelopment.meteorvnclient.gui.screens.accounts.AccountInfoScreen;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorvnclient.systems.accounts.Account;
import meteordevelopment.meteorvnclient.systems.accounts.Accounts;
import meteordevelopment.meteorvnclient.systems.accounts.TokenAccount;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorvnclient.utils.render.color.Color;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public abstract class WAccount extends WHorizontalList {
    public Runnable refreshScreenAction;
    private final WidgetScreen screen;
    private final Account<?> account;

    public WAccount(WidgetScreen screen, Account<?> account) {
        this.screen = screen;
        this.account = account;
    }

    protected abstract Color loggedInColor();
    protected abstract Color accountTypeColor();

    @Override
    public void init() {
        // Head
        add(theme.texture(32, 32, account.getCache().getHeadTexture().needsRotate() ? 90 : 0, account.getCache().getHeadTexture()));

        // Name
        WLabel name = add(theme.label(account.getUsername())).widget();
        if (mc.getSession().getUsername().equalsIgnoreCase(account.getUsername())) name.color = loggedInColor();

        // Type
        WLabel label = add(theme.label("(" + account.getType() + ")")).expandCellX().right().widget();
        label.color = accountTypeColor();

        // Info
        if (account instanceof TokenAccount) {
            WButton info = add(theme.button(TranslationUtils.translate("gui.info", "Info"))).widget();
            info.action = () -> mc.setScreen(new AccountInfoScreen(theme, account));
        }

        // Login
        WButton login = add(theme.button(TranslationUtils.translate("gui.login", "Login"))).widget();
        login.action = () -> {
            login.minWidth = login.width;
            login.set("...");
            screen.locked = true;

            MeteorExecutor.execute(() -> {
                if (account.fetchInfo() && account.login()) {
                    name.set(account.getUsername());

                    Accounts.get().save();

                    screen.taskAfterRender = refreshScreenAction;
                }

                login.minWidth = 0;
                login.set(TranslationUtils.translate("gui.login", "Login"));
                screen.locked = false;
            });
        };

        // Remove
        WMinus remove = add(theme.minus()).widget();
        remove.action = () -> {
            Accounts.get().remove(account);
            if (refreshScreenAction != null) refreshScreenAction.run();
        };
    }
}
