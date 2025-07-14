/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.utils.misc;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import net.minecraft.text.Text;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TranslationUtils {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static boolean loaded = false;

    private TranslationUtils() {
    }

    static {
        loadTranslations();
    }

    private static void loadTranslations() {
        if (loaded) return;

        try {
            InputStream stream = TranslationUtils.class.getResourceAsStream("/assets/meteorvn-client/lang/vi_vn.json");
            if (stream != null) {
                Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8);
                StringBuilder content = new StringBuilder();
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                scanner.close();

                // Parse JSON manually (simple approach)
                String json = content.toString();
                parseTranslations(json);
                loaded = true;
                MeteorVNClient.LOG.info("Loaded {} Vietnamese translations", TRANSLATIONS.size());
            }
        } catch (Exception e) {
            MeteorVNClient.LOG.error("Failed to load Vietnamese translations", e);
        }
    }

    private static void parseTranslations(String json) {
        // Simple JSON parsing for key-value pairs
        String[] lines = json.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains(":") && line.contains("\"")) {
                try {
                    int colonIndex = line.indexOf(":");
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();

                    // Remove quotes and comma
                    key = key.replaceAll("\"", "");
                    value = value.replaceAll("\"", "").replaceAll(",$", "");

                    if (!key.isEmpty() && !value.isEmpty()) {
                        TRANSLATIONS.put(key, value);
                    }
                } catch (Exception e) {
                    // Skip invalid lines
                }
            }
        }
    }

    public static String translate(String key, String fallback) {
        String translation = TRANSLATIONS.get(key);
        return translation != null ? translation : fallback;
    }

    public static Text translateText(String key, String fallback) {
        return Text.literal(translate(key, fallback));
    }

    public static String translate(String key) {
        return translate(key, key);
    }

    public static String translateModule(String moduleName, String fallback) {
        return translate("module." + moduleName.replace("-", "_") + ".description", fallback);
    }

    public static String translateSetting(String settingName, String fallback) {
        return translate("setting." + settingName.replace("-", "_") + ".description", fallback);
    }

    public static String translateMessage(String messageKey, String fallback) {
        return translate("message." + messageKey, fallback);
    }

    public static String translateHud(String hudName, String fallback) {
        return translate("hud." + hudName.replace("-", "_"), fallback);
    }

    public static String translateSettingName(String settingName, String fallback) {
        return translate("setting." + settingName.replace("-", "_"), fallback);
    }

    public static String translateSettingDescription(String settingName, String fallback) {
        return translate("setting." + settingName.replace("-", "_") + ".description", fallback);
    }

    public static String translateEnum(String enumType, String enumValue, String fallback) {
        return translate("enum." + enumType.replace("-", "_") + "." + enumValue.replace("-", "_"), fallback);
    }

    public static String translateGroup(String groupName, String fallback) {
        return translate("group." + groupName.replace("-", "_"), fallback);
    }
}
