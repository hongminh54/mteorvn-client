/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.hud.elements;

import meteordevelopment.meteorvnclient.systems.hud.Hud;
import meteordevelopment.meteorvnclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;

public class MeteorTextHud {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(Hud.GROUP, "text", "Displays arbitrary text with Starscript.", MeteorTextHud::create);

    public static final HudElementInfo<TextHud>.Preset FPS;
    public static final HudElementInfo<TextHud>.Preset TPS;
    public static final HudElementInfo<TextHud>.Preset PING;
    public static final HudElementInfo<TextHud>.Preset SPEED;
    public static final HudElementInfo<TextHud>.Preset GAME_MODE;
    public static final HudElementInfo<TextHud>.Preset DURABILITY;
    public static final HudElementInfo<TextHud>.Preset POSITION;
    public static final HudElementInfo<TextHud>.Preset OPPOSITE_POSITION;
    public static final HudElementInfo<TextHud>.Preset LOOKING_AT;
    public static final HudElementInfo<TextHud>.Preset LOOKING_AT_WITH_POSITION;
    public static final HudElementInfo<TextHud>.Preset BREAKING_PROGRESS;
    public static final HudElementInfo<TextHud>.Preset SERVER;
    public static final HudElementInfo<TextHud>.Preset BIOME;
    public static final HudElementInfo<TextHud>.Preset WORLD_TIME;
    public static final HudElementInfo<TextHud>.Preset REAL_TIME;
    public static final HudElementInfo<TextHud>.Preset ROTATION;
    public static final HudElementInfo<TextHud>.Preset MODULE_ENABLED;
    public static final HudElementInfo<TextHud>.Preset MODULE_ENABLED_WITH_INFO;
    public static final HudElementInfo<TextHud>.Preset WATERMARK;
    public static final HudElementInfo<TextHud>.Preset BARITONE;

    static {
        addPreset("Empty", null);
        FPS = addPreset("FPS", TranslationUtils.translateHud("fps", "FPS: #1{fps}"), 0);
        TPS = addPreset("TPS", TranslationUtils.translateHud("tps", "TPS: #1{round(server.tps, 1)}"));
        PING = addPreset("Ping", TranslationUtils.translateHud("ping", "Ping: #1{ping}"));
        SPEED = addPreset("Speed", TranslationUtils.translateHud("speed", "Tốc độ: #1{round(player.speed, 1)}"), 0);
        GAME_MODE = addPreset("Game mode", TranslationUtils.translateHud("game_mode", "Chế độ chơi: #1{player.gamemode}"), 0);
        DURABILITY = addPreset("Durability", TranslationUtils.translateHud("durability", "Độ bền: #1{player.hand_or_offhand.durability}"));
        POSITION = addPreset("Position", TranslationUtils.translateHud("position", "Vị trí: #1{floor(camera.pos.x)}, {floor(camera.pos.y)}, {floor(camera.pos.z)}"), 0);
        OPPOSITE_POSITION = addPreset("Opposite Position", "{player.opposite_dimension != \"End\" ? player.opposite_dimension + \":\" : \"\"} #1{player.opposite_dimension != \"End\" ? \"\" + floor(camera.opposite_dim_pos.x) + \", \" + floor(camera.opposite_dim_pos.y) + \", \" + floor(camera.opposite_dim_pos.z) : \"\"}", 0);
        LOOKING_AT = addPreset("Looking at", TranslationUtils.translateHud("looking_at", "Đang nhìn: #1{crosshair_target.value}"), 0);
        LOOKING_AT_WITH_POSITION = addPreset("Looking at with position", TranslationUtils.translateHud("looking_at_with_position", "Đang nhìn: #1{crosshair_target.value} {crosshair_target.type != \"miss\" ? \"(\" + \"\" + floor(crosshair_target.value.pos.x) + \", \" + floor(crosshair_target.value.pos.y) + \", \" + floor(crosshair_target.value.pos.z) + \")\" : \"\"}"), 0);
        BREAKING_PROGRESS = addPreset("Breaking progress", TranslationUtils.translateHud("breaking_progress", "Tiến độ phá: #1{round(player.breaking_progress * 100)}%"), 0);
        SERVER = addPreset("Server", TranslationUtils.translateHud("server", "Server: #1{server}"));
        BIOME = addPreset("Biome", TranslationUtils.translateHud("biome", "Quần xã: #1{player.biome}"), 0);
        WORLD_TIME = addPreset("World time", TranslationUtils.translateHud("world_time", "Thời gian: #1{server.time}"));
        REAL_TIME = addPreset("Real time", TranslationUtils.translateHud("real_time", "Thời gian: #1{time}"));
        ROTATION = addPreset("Rotation", TranslationUtils.translateHud("rotation", "{camera.direction} #1({round(camera.yaw, 1)}, {round(camera.pitch, 1)})"), 0);
        MODULE_ENABLED = addPreset("Module enabled", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"BẬT\" : #3 \"TẮT\"}", 0);
        MODULE_ENABLED_WITH_INFO = addPreset("Module enabled with info", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"BẬT\" : #3 \"TẮT\"} #1{meteor.get_module_info(\"kill-aura\")}", 0);
        WATERMARK = addPreset("Watermark", TranslationUtils.translateHud("watermark", "{meteor.name} #1{meteor.version}"));
        BARITONE = addPreset("Baritone", TranslationUtils.translateHud("baritone", "Baritone: #1{baritone.process_name}"));
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text, int updateDelay) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            if (updateDelay != -1) textHud.updateDelay.set(updateDelay);
        });
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return addPreset(title, text, -1);
    }
}
