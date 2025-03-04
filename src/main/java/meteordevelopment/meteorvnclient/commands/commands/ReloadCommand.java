/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorvnclient.commands.Command;
import meteordevelopment.meteorvnclient.renderer.Fonts;
import meteordevelopment.meteorvnclient.systems.Systems;
import meteordevelopment.meteorvnclient.systems.friends.Friend;
import meteordevelopment.meteorvnclient.systems.friends.Friends;
import meteordevelopment.meteorvnclient.utils.network.Capes;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import net.minecraft.command.CommandSource;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            warning("Reloading systems, this may take a while.");

            Systems.load();
            Capes.init();
            Fonts.refresh();
            MeteorExecutor.execute(() -> Friends.get().forEach(Friend::updateInfo));

            return SINGLE_SUCCESS;
        });
    }
}
