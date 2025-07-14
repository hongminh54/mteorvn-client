/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.tabs.builtin;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.gui.tabs.Tab;
import meteordevelopment.meteorvnclient.gui.tabs.TabScreen;
import meteordevelopment.meteorvnclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorvnclient.systems.profiles.Profile;
import meteordevelopment.meteorvnclient.systems.profiles.Profiles;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class ProfilesTab extends Tab {
    public ProfilesTab() {
        super("Profiles");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new ProfilesScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof ProfilesScreen;
    }

    private static class ProfilesScreen extends WindowTabScreen {
        public ProfilesScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            // Create
            WButton create = add(theme.button(TranslationUtils.translate("gui.create", "Create"))).expandX().widget();
            create.action = () -> mc.setScreen(new EditProfileScreen(theme, null, this::reload));
        }

        private void initTable(WTable table) {
            table.clear();
            if (Profiles.get().isEmpty()) return;

            for (Profile profile : Profiles.get()) {
                table.add(theme.label(profile.name.get())).expandCellX();

                WButton save = table.add(theme.button(TranslationUtils.translate("gui.save", "Save"))).widget();
                save.action = profile::save;

                WButton load = table.add(theme.button("Load")).widget();
                load.action = profile::load;

                WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
                edit.action = () -> mc.setScreen(new EditProfileScreen(theme, profile, this::reload));

                WMinus remove = table.add(theme.minus()).widget();
                remove.action = () -> {
                    Profiles.get().remove(profile);
                    reload();
                };

                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Profiles.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Profiles.get());
        }
    }

    private static class EditProfileScreen extends WindowScreen {
        private WContainer settingsContainer;
        private final Profile profile;
        private final boolean isNew;
        private final Runnable action;

        public EditProfileScreen(GuiTheme theme, Profile profile, Runnable action) {
            super(theme, profile == null ? "New Profile" : "Edit Profile");

            this.isNew = profile == null;
            this.profile = isNew ? new Profile() : profile;
            this.action = action;
        }

        @Override
        public void initWidgets() {
            settingsContainer = add(theme.verticalList()).expandX().minWidth(400).widget();
            settingsContainer.add(theme.settings(profile.settings)).expandX();

            add(theme.horizontalSeparator()).expandX();

            WButton save = add(theme.button(isNew ? TranslationUtils.translate("gui.create", "Create") : TranslationUtils.translate("gui.save", "Save"))).expandX().widget();
            save.action = () -> {
                if (profile.name.get().isEmpty()) return;

                if (isNew) {
                    for (Profile p : Profiles.get()) {
                        if (profile.equals(p)) return;
                    }
                }

                List<String> valid = new ArrayList<>();
                for (String address : profile.loadOnJoin.get()) {
                    if (Utils.resolveAddress(address)) valid.add(address);
                }

                profile.loadOnJoin.set(valid);

                if (isNew) Profiles.get().add(profile);
                else Profiles.get().save();

                close();
            };

            enterAction = save.action;
        }

        @Override
        public void tick() {
            profile.settings.tick(settingsContainer, theme);
        }

        @Override
        protected void onClosed() {
            if (action != null) action.run();
        }
    }
}
