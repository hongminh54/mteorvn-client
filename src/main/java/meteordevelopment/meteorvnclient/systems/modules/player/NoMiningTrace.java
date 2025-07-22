/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.player;

import meteordevelopment.meteorvnclient.settings.BoolSetting;
import meteordevelopment.meteorvnclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.settings.SettingGroup;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.PickaxeItem;

import java.util.Set;

public class NoMiningTrace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("blacklisted-entities")
        .description("Entities you will interact with as normal.")
        .defaultValue()
        .build()
    );

    private final Setting<Boolean> onlyWhenHoldingPickaxe = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-holding-a-pickaxe")
        .description("Whether or not to work only when holding a pickaxe.")
        .defaultValue(true)
        .build()
    );

    public NoMiningTrace() {
        super(Categories.Player, "no-mining-trace", "Allows you to mine blocks through entities.");
    }

    public boolean canWork(Entity entity) {
        if (!isActive()) return false;

        return (!onlyWhenHoldingPickaxe.get() || mc.player.getMainHandStack().getItem() instanceof PickaxeItem || mc.player.getOffHandStack().getItem() instanceof PickaxeItem) &&
            (entity == null || !entities.get().contains(entity.getType()));
    }
}
