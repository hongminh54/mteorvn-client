/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.player;

import meteordevelopment.meteorvnclient.settings.BoolSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.settings.SettingGroup;
import meteordevelopment.meteorvnclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.player.PlayerUtils;
import net.minecraft.entity.effect.StatusEffect;

import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.*;

public class PotionSaver extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<StatusEffect>> effects = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("effects")
        .description("The effects to preserve.")
        .defaultValue(
            STRENGTH.value(),
            ABSORPTION.value(),
            RESISTANCE.value(),
            FIRE_RESISTANCE.value(),
            SPEED.value(),
            HASTE.value(),
            REGENERATION.value(),
            WATER_BREATHING.value(),
            SATURATION.value(),
            LUCK.value(),
            SLOW_FALLING.value(),
            DOLPHINS_GRACE.value(),
            CONDUIT_POWER.value(),
            HERO_OF_THE_VILLAGE.value()
        )
        .build()
    );

    public final Setting<Boolean> onlyWhenStationary = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-stationary")
        .description("Only freezes effects when you aren't moving.")
        .defaultValue(false)
        .build()
    );

    public PotionSaver() {
        super(Categories.Player, "potion-saver", "Stops potion effects ticking when you stand still.");
    }

    public boolean shouldFreeze(StatusEffect effect) {
        return isActive() && (!onlyWhenStationary.get() || !PlayerUtils.isMoving()) && !mc.player.getStatusEffects().isEmpty() && effects.get().contains(effect);
    }
}
