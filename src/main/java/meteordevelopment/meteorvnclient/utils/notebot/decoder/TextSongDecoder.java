/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.utils.notebot.decoder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import meteordevelopment.meteorvnclient.utils.notebot.song.Note;
import meteordevelopment.meteorvnclient.utils.notebot.song.Song;
import net.minecraft.block.enums.NoteBlockInstrument;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TextSongDecoder extends SongDecoder {

    @Override
    public Song parse(File file) throws Exception {
        List<String> data = Files.readAllLines(file.toPath());

        Multimap<Integer, Note> notesMap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        String title = FilenameUtils.getBaseName(file.getName());
        String author = "Unknown";

        for (int lineNumber = 0; lineNumber < data.size(); lineNumber++) {
            String line = data.get(lineNumber);
            if (line.startsWith("// Name: ")) {
                title = line.substring(9);
                continue;
            }
            if (line.startsWith("// Author: ")) {
                author = line.substring(11);
                continue;
            }
            if (line.isEmpty()) continue;

            String[] parts = data.get(lineNumber).split(":");
            if (parts.length < 2) {
                notebot.warning("Malformed line %d", lineNumber);
                continue;
            }
            int key;
            int val;
            int type = 0;
            try {
                key = Integer.parseInt(parts[0]);
                val = Integer.parseInt(parts[1]);
                if (parts.length > 2) {
                    type = Integer.parseInt(parts[2]);
                }
            } catch (NumberFormatException e) {
                notebot.warning("Invalid character at line %d", lineNumber);
                continue;
            }

            Note note = new Note(NoteBlockInstrument.values()[type], val);
            notesMap.put(key, note);
        }
        return new Song(notesMap, title, author);
    }
}
