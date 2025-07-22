/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens.settings;

import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.settings.BlockDataSetting;
import meteordevelopment.meteorvnclient.settings.IBlockData;
import meteordevelopment.meteorvnclient.utils.misc.IChangeable;
import meteordevelopment.meteorvnclient.utils.misc.ICopyable;
import meteordevelopment.meteorvnclient.utils.misc.ISerializable;
import meteordevelopment.meteorvnclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class BlockDataSettingScreen extends WindowScreen {
    private static final List<Block> BLOCKS = new ArrayList<>(100);

    private final BlockDataSetting<?> setting;

    private WTable table;
    private String filterText = "";

    public BlockDataSettingScreen(GuiTheme theme, BlockDataSetting<?> setting) {
        super(theme, "Configure Blocks");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> void initTable() {
        for (Block block : Registries.BLOCK) {
            T blockData = (T) setting.get().get(block);

            if (blockData != null && blockData.isChanged()) BLOCKS.addFirst(block);
            else BLOCKS.add(block);
        }

        for (Block block : BLOCKS) {
            String name = Names.get(block);
            if (!StringUtils.containsIgnoreCase(name, filterText)) continue;

            T blockData = (T) setting.get().get(block);

            table.add(theme.itemWithLabel(block.asItem().getDefaultStack(), Names.get(block))).expandCellX();
            table.add(theme.label((blockData != null && blockData.isChanged()) ? "*" : " "));

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                T data = blockData;
                if (data == null) data = (T) setting.defaultData.get().copy();

                mc.setScreen(data.createScreen(theme, block, (BlockDataSetting<T>) setting));
            };

            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.get().remove(block);
                setting.onChanged();

                if (blockData != null && blockData.isChanged()) {
                    table.clear();
                    initTable();
                }
            };

            table.row();
        }

        BLOCKS.clear();
    }
}
