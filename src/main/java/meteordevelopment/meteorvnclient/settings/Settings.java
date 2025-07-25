/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.misc.ISerializable;
import meteordevelopment.meteorvnclient.utils.misc.NbtUtils;
import meteordevelopment.meteorvnclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorvnclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Settings implements ISerializable<Settings>, Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    public final List<SettingGroup> groups = new ArrayList<>(1);

    public void onActivated() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                setting.onActivated();
            }
        }
    }

    public Setting<?> get(String name) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                if (name.equalsIgnoreCase(setting.name)) return setting;
            }
        }

        return null;
    }

    public void reset() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                setting.reset();
            }
        }
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup sg : this) {
            if (sg.name.equals(name)) return sg;
        }

        return null;
    }

    public int sizeGroups() {
        return groups.size();
    }

    public SettingGroup getDefaultGroup() {
        if (defaultGroup == null) defaultGroup = createGroup("General");
        return defaultGroup;
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        groups.add(group);
        return group;
    }
    public SettingGroup createGroup(String name) {
        return createGroup(name, true);
    }

    public void registerColorSettings(Module module) {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                setting.module = module;

                if (setting instanceof ColorSetting) {
                    RainbowColors.addSetting((Setting<SettingColor>) setting);
                }
                else if (setting instanceof ColorListSetting) {
                    RainbowColors.addSettingList((Setting<List<SettingColor>>) setting);
                }
            }
        }
    }

    public void unregisterColorSettings() {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                if (setting instanceof ColorSetting) {
                    RainbowColors.removeSetting((Setting<SettingColor>) setting);
                }
                else if (setting instanceof ColorListSetting) {
                    RainbowColors.removeSettingList((Setting<List<SettingColor>>) setting);
                }
            }
        }
    }

    public void tick(WContainer settings, GuiTheme theme) {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                boolean visible = setting.isVisible();

                if (visible != setting.lastWasVisible) {
                    settings.clear();
                    settings.add(theme.settings(this)).expandX();
                }

                setting.lastWasVisible = visible;
            }
        }
    }

    @Override
    public @NotNull Iterator<SettingGroup> iterator() {
        return groups.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("groups", NbtUtils.listToTag(groups));

        return tag;
    }

    @Override
    public Settings fromTag(NbtCompound tag) {
        NbtList groupsTag = tag.getList("groups", 10);

        for (NbtElement t : groupsTag) {
            NbtCompound groupTag = (NbtCompound) t;

            SettingGroup sg = getGroup(groupTag.getString("name"));
            if (sg != null) sg.fromTag(groupTag);
        }

        return this;
    }
}
