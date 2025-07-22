/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.addons;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Meteor pseudo addon
        {
            MeteorVNClient.ADDON = new MeteorAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "meteordevelopment.meteorvnclient";
                }

                @Override
                public String getWebsite() {
                    return "https://github.com/hongminh54/mteorvn-client";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo("MeteorDevelopment", "meteorvn-client");
                }

                @Override
                public String getCommit() {
                    String commit = MeteorVNClient.MOD_META.getCustomValue(MeteorVNClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(MeteorVNClient.MOD_ID).get().getMetadata();

            MeteorVNClient.ADDON.name = metadata.getName();
            MeteorVNClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(MeteorVNClient.MOD_ID + ":color")) {
                MeteorVNClient.ADDON.color.parse(metadata.getCustomValue(MeteorVNClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                MeteorVNClient.ADDON.authors[i++] = author.getName();
            }

            ADDONS.add(MeteorVNClient.ADDON);
        }

        // Addons
        for (EntrypointContainer<MeteorAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("meteor", MeteorAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            MeteorAddon addon;
            try {
                addon = entrypoint.getEntrypoint();
            } catch (Throwable throwable) {
                throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata.getName()), throwable);
            }

            addon.name = metadata.getName();

            if (metadata.getAuthors().isEmpty()) throw new RuntimeException("Addon \"%s\" requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
            addon.authors = new String[metadata.getAuthors().size()];

            if (metadata.containsCustomValue(MeteorVNClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(MeteorVNClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
