/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorvnclient.commands.Command;
import meteordevelopment.meteorvnclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorvnclient.systems.hud.Hud;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
            .then(literal("all")
                .then(literal("on")
                    .executes(context -> {
                        new ArrayList<>(Modules.get().getAll()).forEach(module -> {
                            if (!module.isActive()) module.toggle();
                        });
                        Hud.get().active = true;
                        return SINGLE_SUCCESS;
                    })
                )
                .then(literal("off")
                    .executes(context -> {
                        new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);
                        Hud.get().active = false;
                        return SINGLE_SUCCESS;
                    })
                )
            )
            .then(argument("module", ModuleArgumentType.create())
                .executes(context -> {
                    Module m = ModuleArgumentType.get(context);
                    m.toggle();
                    m.sendToggledMsg();
                    return SINGLE_SUCCESS;
                })
                .then(literal("on")
                    .executes(context -> {
                        Module m = ModuleArgumentType.get(context);
                        if (!m.isActive()) m.toggle();
                        return SINGLE_SUCCESS;
                    }))
                .then(literal("off")
                    .executes(context -> {
                        Module m = ModuleArgumentType.get(context);
                        if (m.isActive()) m.toggle();
                        return SINGLE_SUCCESS;
                    })
                )
            )
            .then(literal("hud")
                .executes(context -> {
                    Hud.get().active = !(Hud.get().active);
                    return SINGLE_SUCCESS;
                })
                .then(literal("on")
                    .executes(context -> {
                        Hud.get().active = true;
                        return SINGLE_SUCCESS;
                    })
                ).then(literal("off")
                    .executes(context -> {
                        Hud.get().active = false;
                        return SINGLE_SUCCESS;
                    })
                )
            );
    }
}
