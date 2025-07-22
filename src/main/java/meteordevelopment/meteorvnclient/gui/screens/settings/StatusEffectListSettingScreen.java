/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.Optional;

public class StatusEffectListSettingScreen extends RegistryListSettingScreen<StatusEffect> {
    public StatusEffectListSettingScreen(GuiTheme theme, Setting<List<StatusEffect>> setting) {
        super(theme, "Select Effects", setting, setting.get(), Registries.STATUS_EFFECT);
    }

    @Override
    protected WWidget getValueWidget(StatusEffect value) {
        return theme.itemWithLabel(getPotionStack(value), getValueName(value));
    }

    @Override
    protected String getValueName(StatusEffect value) {
        return Names.get(value);
    }

    private ItemStack getPotionStack(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();

        potion.set(
            DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(
                potion.get(DataComponentTypes.POTION_CONTENTS).potion(),
                Optional.of(effect.getColor()),
                potion.get(DataComponentTypes.POTION_CONTENTS).customEffects(),
                Optional.empty()
            )
        );

        return potion;
    }
}
