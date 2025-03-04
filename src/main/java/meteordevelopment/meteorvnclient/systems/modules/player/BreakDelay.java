/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.player;

import meteordevelopment.meteorvnclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorvnclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorvnclient.settings.BoolSetting;
import meteordevelopment.meteorvnclient.settings.IntSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.settings.SettingGroup;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

public class BreakDelay extends Module {
    SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown")
        .description("Block break cooldown in ticks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(5)
        .build()
    );

    private final Setting<Boolean> noInstaBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("no-insta-break")
        .description("Prevents you from misbreaking blocks if you can instantly break them.")
        .defaultValue(false)
        .build()
    );

    private boolean breakBlockCooldown = false;

    public BreakDelay() {
        super(Categories.Player, "break-delay", "Changes the delay between breaking blocks.");
    }

    @EventHandler
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        if (breakBlockCooldown) {
            event.cooldown = 5;
            breakBlockCooldown = false;
        } else {
            event.cooldown = cooldown.get();
        }
    }

    @EventHandler
    private void onClick(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && noInstaBreak.get()) {
            breakBlockCooldown = true;
        }
    }

    public boolean preventInstaBreak() {
        return isActive() && noInstaBreak.get();
    }
}
