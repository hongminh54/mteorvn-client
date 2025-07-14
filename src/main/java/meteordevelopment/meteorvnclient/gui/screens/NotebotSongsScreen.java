/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.notebot.decoder.SongDecoders;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotebotSongsScreen extends WindowScreen {
    private static final Notebot notebot = Modules.get().get(Notebot.class);

    private WTextBox filter;
    private String filterText = "";

    private WTable table;

    public NotebotSongsScreen(GuiTheme theme) {
        super(theme, TranslationUtils.translate("gui.notebot_songs", "Notebot Songs"));
    }

    @Override
    public void initWidgets() {
        // Random Song
        WButton randomSong = add(theme.button(TranslationUtils.translate("gui.random_song", "Random Song"))).minWidth(400).expandX().widget();
        randomSong.action = notebot::playRandomSong;

        // Filter
        filter = add(theme.textBox("", TranslationUtils.translate("gui.search_songs", "Search for the songs..."))).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initSongsTable();
        };

        table = add(theme.table()).widget();

        initSongsTable();
    }

    private void initSongsTable() {
        AtomicBoolean noSongsFound = new AtomicBoolean(true);
        try {
            Files.list(MeteorVNClient.FOLDER.toPath().resolve("notebot")).forEach(path -> {
                if (SongDecoders.hasDecoder(path)) {
                    String name = path.getFileName().toString();

                    if (Utils.searchTextDefault(name, filterText, false)){
                        addPath(path);
                        noSongsFound.set(false);
                    }
                }
            });
        } catch (IOException e) {
            table.add(theme.label(TranslationUtils.translate("gui.missing_notebot_folder", "Missing meteorvn-client/notebot folder."))).expandCellX();
            table.row();
        }

        if (noSongsFound.get()) {
            table.add(theme.label(TranslationUtils.translate("gui.no_songs_found", "No songs found."))).expandCellX().center();
        }
    }

    private void addPath(Path path) {
        table.add(theme.horizontalSeparator()).expandX().minWidth(400);
        table.row();

        table.add(theme.label(FilenameUtils.getBaseName(path.getFileName().toString()))).expandCellX();
        WButton load = table.add(theme.button(TranslationUtils.translate("gui.load", "Load"))).right().widget();
        load.action = () -> notebot.loadSong(path.toFile());
        WButton preview = table.add(theme.button(TranslationUtils.translate("gui.preview", "Preview"))).right().widget();
        preview.action = () -> notebot.previewSong(path.toFile());

        table.row();
    }
}
