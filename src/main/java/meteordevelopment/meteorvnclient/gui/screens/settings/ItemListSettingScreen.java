/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.settings.ItemListSetting;
import meteordevelopment.meteorvnclient.utils.misc.Names;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.function.Predicate;

public class ItemListSettingScreen extends RegistryListSettingScreen<Item> {
    public ItemListSettingScreen(GuiTheme theme, ItemListSetting setting) {
        super(theme, "Select Items", setting, setting.get(), Registries.ITEM);
    }

    @Override
    protected boolean includeValue(Item value) {
        Predicate<Item> filter = ((ItemListSetting) setting).filter;
        if (filter != null && !filter.test(value)) return false;

        return value != Items.AIR;
    }

    @Override
    protected WWidget getValueWidget(Item value) {
        return theme.itemWithLabel(value.getDefaultStack());
    }

    @Override
    protected String getValueName(Item value) {
        return Names.get(value);
    }
}
