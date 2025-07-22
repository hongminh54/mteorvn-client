/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorvnclient.commands.Command;
import meteordevelopment.meteorvnclient.commands.arguments.MacroArgumentType;
import meteordevelopment.meteorvnclient.systems.macros.Macro;
import net.minecraft.command.CommandSource;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "Allows you to execute macros.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("macro", MacroArgumentType.create()).executes(context -> {
            Macro macro = MacroArgumentType.get(context);
            macro.onAction();
            return SINGLE_SUCCESS;
        }));
    }
}
