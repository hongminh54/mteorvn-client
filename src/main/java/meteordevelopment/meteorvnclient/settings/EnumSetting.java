/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.settings;

import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    private final T[] values;

    private final List<String> suggestions;

    public EnumSetting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        values = (T[]) defaultValue.getDeclaringClass().getEnumConstants();
        suggestions = new ArrayList<>(values.length);
        for (T value : values) {
            String enumClassName = defaultValue.getDeclaringClass().getSimpleName().toLowerCase();
            String enumValueName = value.toString().toLowerCase();
            String translatedValue = TranslationUtils.translateEnum(enumClassName, enumValueName, value.toString());
            suggestions.add(translatedValue);
        }
    }

    @Override
    protected T parseImpl(String str) {
        for (T possibleValue : values) {
            if (str.equalsIgnoreCase(possibleValue.toString())) return possibleValue;
        }

        return null;
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", get().toString());

        return tag;
    }

    @Override
    public T load(NbtCompound tag) {
        parse(tag.getString("value"));

        return get();
    }

    public static class Builder<T extends Enum<?>> extends SettingBuilder<Builder<T>, T, EnumSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public EnumSetting<T> build() {
            return new EnumSetting<>(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
