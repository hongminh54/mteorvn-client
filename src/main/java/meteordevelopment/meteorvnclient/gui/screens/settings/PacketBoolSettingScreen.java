/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.settings.PacketListSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.utils.network.PacketUtils;
import net.minecraft.network.packet.Packet;

import java.util.Set;
import java.util.function.Predicate;

public class PacketBoolSettingScreen extends RegistryListSettingScreen<Class<? extends Packet<?>>> {
    public PacketBoolSettingScreen(GuiTheme theme, Setting<Set<Class<? extends Packet<?>>>> setting) {
        super(theme, "Select Packets", setting, setting.get(), PacketUtils.REGISTRY);
    }

    @Override
    protected boolean includeValue(Class<? extends Packet<?>> value) {
        Predicate<Class<? extends Packet<?>>> filter = ((PacketListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Class<? extends Packet<?>> value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(Class<? extends Packet<?>> value) {
        return PacketUtils.getName(value);
    }
}
