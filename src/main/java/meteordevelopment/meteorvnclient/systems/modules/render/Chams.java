/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.render;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.settings.*;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.render.color.SettingColor;
import meteordevelopment.meteorvnclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.Set;

public class Chams extends Module {
    private final SettingGroup sgThroughWalls = settings.createGroup("Through Walls");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgCrystals = settings.createGroup("Crystals");
    private final SettingGroup sgHand = settings.createGroup("Hand");

    // Through walls

    public final Setting<Set<EntityType<?>>> entities = sgThroughWalls.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to show through walls.")
        .onlyAttackable()
        .build()
    );

    public final Setting<Shader> shader = sgThroughWalls.add(new EnumSetting.Builder<Shader>()
        .name("shader")
        .description("Renders a shader over of the entities.")
        .defaultValue(Shader.Image)
        .onModuleActivated(setting -> updateShader(setting.get()))
        .onChanged(this::updateShader)
        .build()
    );

    public final Setting<SettingColor> shaderColor = sgThroughWalls.add(new ColorSetting.Builder()
        .name("color")
        .description("The color that the shader is drawn with.")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .visible(() -> shader.get() != Shader.None)
        .build()
    );

    public final Setting<Boolean> ignoreSelfDepth = sgThroughWalls.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Ignores yourself drawing the player.")
        .defaultValue(true)
        .build()
    );

    // Players

    public final Setting<Boolean> players = sgPlayers.add(new BoolSetting.Builder()
        .name("players")
        .description("Enables model tweaks for players.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> ignoreSelf = sgPlayers.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Ignores yourself when tweaking player models.")
        .defaultValue(false)
        .visible(players::get)
        .build()
    );

    public final Setting<Boolean> playersTexture = sgPlayers.add(new BoolSetting.Builder()
        .name("texture")
        .description("Enables player model textures.")
        .defaultValue(false)
        .visible(players::get)
        .build()
    );

    public final Setting<SettingColor> playersColor = sgPlayers.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of player models.")
        .defaultValue(new SettingColor(198, 135, 254, 150))
        .visible(players::get)
        .build()
    );

    public final Setting<Double> playersScale = sgPlayers.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Players scale.")
        .defaultValue(1.0)
        .min(0.0)
        .visible(players::get)
        .build()
    );

    // Crystals

    public final Setting<Boolean> crystals = sgCrystals.add(new BoolSetting.Builder()
        .name("crystals")
        .description("Enables model tweaks for end crystals.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> crystalsScale = sgCrystals.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Crystal scale.")
        .defaultValue(0.6)
        .min(0)
        .visible(crystals::get)
        .build()
    );

    public final Setting<Double> crystalsBounce = sgCrystals.add(new DoubleSetting.Builder()
        .name("bounce")
        .description("How high crystals bounce.")
        .defaultValue(0.6)
        .min(0.0)
        .visible(crystals::get)
        .build()
    );

    public final Setting<Double> crystalsRotationSpeed = sgCrystals.add(new DoubleSetting.Builder()
        .name("rotation-speed")
        .description("Multiplies the rotation speed of the crystal.")
        .defaultValue(0.3)
        .min(0)
        .visible(crystals::get)
        .build()
    );

    public final Setting<Boolean> crystalsTexture = sgCrystals.add(new BoolSetting.Builder()
        .name("texture")
        .description("Whether to render crystal model textures.")
        .defaultValue(true)
        .visible(crystals::get)
        .build()
    );

    public final Setting<SettingColor> crystalsColor = sgCrystals.add(new ColorSetting.Builder()
        .name("crystal-color")
        .description("The color of the of the crystal.")
        .defaultValue(new SettingColor(198, 135, 254, 255))
        .visible(crystals::get)
        .build()
    );

    // Hand

    public final Setting<Boolean> hand = sgHand.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tweaks of hand rendering.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> handTexture = sgHand.add(new BoolSetting.Builder()
        .name("texture")
        .description("Whether to render hand textures.")
        .defaultValue(false)
        .visible(hand::get)
        .build()
    );

    public final Setting<SettingColor> handColor = sgHand.add(new ColorSetting.Builder()
        .name("hand-color")
        .description("The color of your hand.")
        .defaultValue(new SettingColor(198, 135, 254, 150))
        .visible(hand::get)
        .build()
    );

    public static final Identifier BLANK = MeteorVNClient.identifier("textures/blank.png");

    public Chams() {
        super(Categories.Render, "chams", "Tweaks rendering of entities.");
    }

    public boolean shouldRender(Entity entity) {
        return isActive() && !isShader() && entities.get().contains(entity.getType()) && (entity != mc.player || !ignoreSelfDepth.get());
    }

    public boolean isShader() {
        return isActive() && shader.get() != Shader.None;
    }

    public void updateShader(Shader value) {
        if (value == Shader.None) return;
        PostProcessShaders.CHAMS.init(Utils.titleToName(value.name()));
    }

    public enum Shader {
        Image,
        None
    }
}
