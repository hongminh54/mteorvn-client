/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.movement;

import meteordevelopment.meteorvnclient.events.world.TickEvent;
import meteordevelopment.meteorvnclient.pathing.NopPathManager;
import meteordevelopment.meteorvnclient.pathing.PathManagers;
import meteordevelopment.meteorvnclient.settings.EnumSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.settings.SettingGroup;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.KeyBinding;

public class AutoWalk extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Walking mode.")
        .defaultValue(Mode.Smart)
        .onChanged(mode1 -> {
            if (isActive() && Utils.canUpdate()) {
                if (mode1 == Mode.Simple) {
                    PathManagers.get().stop();
                } else {
                    createGoal();
                }

                unpress();
            }
        })
        .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("simple-direction")
        .description("The direction to walk in simple mode.")
        .defaultValue(Direction.Forwards)
        .onChanged(direction1 -> {
            if (isActive()) unpress();
        })
        .visible(() -> mode.get() == Mode.Simple)
        .build()
    );

    public AutoWalk() {
        super(Categories.Movement, "auto-walk", "Automatically walks forward.");
    }

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Smart) createGoal();
    }

    @Override
    public void onDeactivate() {
        if (mode.get() == Mode.Simple) unpress();
        else PathManagers.get().stop();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (mode.get() == Mode.Simple) {
            switch (direction.get()) {
                case Forwards -> setPressed(mc.options.forwardKey, true);
                case Backwards -> setPressed(mc.options.backKey, true);
                case Left -> setPressed(mc.options.leftKey, true);
                case Right -> setPressed(mc.options.rightKey, true);
            }
        } else {
            if (PathManagers.get() instanceof NopPathManager) {
                info("Smart mode requires Baritone");
                toggle();
            }
        }
    }

    private void unpress() {
        setPressed(mc.options.forwardKey, false);
        setPressed(mc.options.backKey, false);
        setPressed(mc.options.leftKey, false);
        setPressed(mc.options.rightKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    private void createGoal() {
        PathManagers.get().moveInDirection(mc.player.getYaw());
    }

    public enum Mode {
        Simple,
        Smart
    }

    public enum Direction {
        Forwards,
        Backwards,
        Left,
        Right
    }
}
