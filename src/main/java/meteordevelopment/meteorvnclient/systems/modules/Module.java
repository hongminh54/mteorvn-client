/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.addons.AddonManager;
import meteordevelopment.meteorvnclient.addons.MeteorAddon;
import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.settings.Settings;
import meteordevelopment.meteorvnclient.systems.config.Config;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.ISerializable;
import meteordevelopment.meteorvnclient.utils.misc.Keybind;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.player.ChatUtils;
import meteordevelopment.meteorvnclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Module implements ISerializable<Module>, Comparable<Module> {
    protected final MinecraftClient mc;

    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final String[] aliases;
    public final Color color;

    public final MeteorAddon addon;
    public final Settings settings = new Settings();

    private boolean active;

    public boolean serialize = true;
    public boolean runInMainMenu = false;
    public boolean autoSubscribe = true;

    public final Keybind keybind = Keybind.none();
    public boolean toggleOnBindRelease = false;
    public boolean chatFeedback = true;
    public boolean favorite = false;

    public Module(Category category, String name, String description, String... aliases) {
        if (name.contains(" ")) MeteorVNClient.LOG.warn("Module '{}' contains invalid characters in its name making it incompatible with MeteorVN Client commands.", name);

        this.mc = MinecraftClient.getInstance();
        this.category = category;
        this.name = new String(name.getBytes(java.nio.charset.StandardCharsets.UTF_8), java.nio.charset.StandardCharsets.UTF_8);
        this.title = Utils.nameToTitle(name);
        this.description = TranslationUtils.translateModule(name, description);
        this.aliases = aliases;
        this.color = Color.fromHsv(Utils.random(0.0, 360.0), 0.35, 1);

        String classname = this.getClass().getName();
        for (MeteorAddon addon : AddonManager.ADDONS) {
            if (classname.startsWith(addon.getPackage())) {
                this.addon = addon;
                return;
            }
        }

        this.addon = null;
    }

    public Module(Category category, String name, String desc) {
        this(category, name, desc, new String[0]);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    public void onActivate() {}
    public void onDeactivate() {}

    public void toggle() {
        if (!active) {
            active = true;
            Modules.get().addActive(this);

            settings.onActivated();

            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) MeteorVNClient.EVENT_BUS.subscribe(this);
                onActivate();
            }
        }
        else {
            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) MeteorVNClient.EVENT_BUS.unsubscribe(this);
                onDeactivate();
            }

            active = false;
            Modules.get().removeActive(this);
        }
    }

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback) {
            ChatUtils.forceNextPrefixClass(getClass());
            ChatUtils.sendMsg(this.hashCode(), Formatting.GRAY, "%s(default) %s(default)(highlight).", title, isActive() ? Formatting.GREEN + TranslationUtils.translateMessage("enabled", "Bật") : Formatting.RED + TranslationUtils.translateMessage("disabled", "Tắt"));
        }
    }

    public void info(Text message) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(title, message);
    }

    public void info(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.infoPrefix(title, message, args);
    }

    public void warning(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(title, message, args);
    }

    public void error(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(title, message, args);
    }

    public boolean isActive() {
        return active;
    }

    public String getInfoString() {
        return null;
    }

    @Override
    public NbtCompound toTag() {
        if (!serialize) return null;
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        tag.put("keybind", keybind.toTag());
        tag.putBoolean("toggleOnKeyRelease", toggleOnBindRelease);
        tag.putBoolean("chatFeedback", chatFeedback);
        tag.putBoolean("favorite", favorite);
        tag.put("settings", settings.toTag());
        tag.putBoolean("active", active);

        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        // General
        keybind.fromTag(tag.getCompound("keybind"));
        toggleOnBindRelease = tag.getBoolean("toggleOnKeyRelease");
        chatFeedback = !tag.contains("chatFeedback") || tag.getBoolean("chatFeedback");
        favorite = tag.getBoolean("favorite");

        // Settings
        NbtElement settingsTag = tag.get("settings");
        if (settingsTag instanceof NbtCompound) settings.fromTag((NbtCompound) settingsTag);

        boolean active = tag.getBoolean("active");
        if (active != isActive()) toggle();

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return name.compareTo(o.name);
    }
}
