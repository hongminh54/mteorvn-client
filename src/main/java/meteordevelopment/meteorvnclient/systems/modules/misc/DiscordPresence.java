/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.misc;

//Created by squidoodly
//Recode by hongminh54

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorvnclient.events.world.TickEvent;
import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WidgetScreen;
import meteordevelopment.meteorvnclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorvnclient.settings.*;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.*;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorvnclient.MeteorVNClient.IN_DEVELOPMENT;

public class DiscordPresence extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }

    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");

    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
        .name("tin-nhan-dong-1")
        .description("Các tin nhắn được sử dụng cho dòng 1.")
        .defaultValue("{player}", "{server}")
        .onChanged(strings -> recompileLine1())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
        .name("toc-do-cap-nhap-dong-1")
        .description("Tốc độ cập nhật tin nhắn cho dòng 1 tính bằng tick.")
        .defaultValue(200)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
        .name("che-do-chon-dong-1")
        .description("Cách chọn tin nhắn cho dòng 1.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
        .name("Tin-nhan-dong-2")
        .description("Các tin nhắn được sử dụng cho dòng 2.")
        .defaultValue("MeteorVN cây nhà lá vườn!", "Rework by hongminh54")
        .onChanged(strings -> recompileLine2())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
        .name("do-tre-cap-nhat-dong-2")
        .description("Tốc độ cập nhật tin nhắn cho dòng 2 tính bằng tick.")
        .defaultValue(60)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
        .name("Che-do-chon-dong-2")
        .description("Cách chọn tin nhắn cho dòng 2.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    private static final RichPresence rpc = new RichPresence();
    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    public static final List<Pair<String, String>> customStates = new ArrayList<>();

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Duyệt mod");
        registerCustomState("me.jellysquid.mods.sodium.client", "Thay đổi tùy chọn");
    }

    public DiscordPresence() {
        super(Categories.Misc, "discord-presence", "Hiển thị trạng thái của bạn trên Discord.");

        runInMainMenu = true;
    }

    /** Registers a custom state to be used when the current screen is a class in the specified package. */
    public static void registerCustomState(String packageName, String state) {
        for (var pair : customStates) {
            if (pair.getLeft().equals(packageName)) {
                pair.setRight(state);
                return;
            }
        }

        customStates.add(new Pair<>(packageName, state));
    }

    /** The package name must match exactly to the one provided through {@link #registerCustomState(String, String)}. */
    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> pair.getLeft().equals(packageName));
    }

    @Override
    public void onActivate() {
        DiscordIPC.start(1256887737512890458L, null);

        rpc.setStart(System.currentTimeMillis() / 1000L);

        String largeText = "%s %s".formatted("MeteorVN Client", MeteorVNClient.VERSION);
        if (!MeteorVNClient.BUILD_NUMBER.isEmpty()) largeText += " DevBuild: " + MeteorVNClient.BUILD_NUMBER;
        rpc.setLargeImage("meteorvn_client", largeText);

        currentSmallImage = SmallImage.hongminh54;

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (String message : messages) {
            Script script = MeteorStarscript.compile(message);
            if (script != null) scripts.add(script);
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;

        // Image
        if (ticks >= 20 || forceUpdate) {
            currentSmallImage = currentSmallImage.next();
            currentSmallImage.apply();
            update = true;

            ticks = 0;
        } else ticks++;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (!line1Scripts.isEmpty()) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    String message = MeteorStarscript.run(line1Scripts.get(i));
                    if (message != null) {
                        message = new String(message.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        rpc.setDetails(message);
                    }
                    update = true;

                    line1Ticks = 0;
                } else line1Ticks++;

                // Line 2
                if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                    if (!line2Scripts.isEmpty()) {
                        int i = Utils.random(0, line2Scripts.size());
                        if (line2SelectMode.get() == SelectMode.Sequential) {
                            if (line2I >= line2Scripts.size()) line2I = 0;
                            i = line2I++;
                        }

                        String message = MeteorStarscript.run(line2Scripts.get(i));
                        if (message != null) {
                            message = new String(message.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                            rpc.setState(message);
                        }
                    }
                    update = true;

                    line2Ticks = 0;
                } else line2Ticks++;
            } else {
                if (!lastWasInMainMenu) {
                    rpc.setDetails("Đang chơi " + MeteorVNClient.NAME + " " + MeteorVNClient.VERSION + (IN_DEVELOPMENT ? " DevBuild: " + MeteorVNClient.DEV_BUILD : ""));

                    if (mc.currentScreen instanceof TitleScreen) rpc.setState("Đang nghe nhạc Ngọt");
                    else if (mc.currentScreen instanceof SelectWorldScreen) rpc.setState("Đang chọn thế giới");
                    else if (mc.currentScreen instanceof CreateWorldScreen || mc.currentScreen instanceof EditGameRulesScreen) rpc.setState("Đang tạo thế giới");
                    else if (mc.currentScreen instanceof EditWorldScreen) rpc.setState("Đang chỉnh sửa thế giới");
                    else if (mc.currentScreen instanceof LevelLoadingScreen) rpc.setState("Đang tải thế giới");
                    else if (mc.currentScreen instanceof MultiplayerScreen) rpc.setState("Đang chọn máy chủ");
                    else if (mc.currentScreen instanceof AddServerScreen) rpc.setState("Đang thêm máy chủ");
                    else if (mc.currentScreen instanceof ConnectScreen || mc.currentScreen instanceof DirectConnectScreen) rpc.setState("Đang kết nối máy chủ");
                    else if (mc.currentScreen instanceof WidgetScreen) rpc.setState("Đang mở GUI mô-đun MeteorVN");
                    else if (mc.currentScreen instanceof OptionsScreen || mc.currentScreen instanceof SkinOptionsScreen || mc.currentScreen instanceof SoundOptionsScreen || mc.currentScreen instanceof VideoOptionsScreen || mc.currentScreen instanceof ControlsOptionsScreen || mc.currentScreen instanceof LanguageOptionsScreen || mc.currentScreen instanceof ChatOptionsScreen || mc.currentScreen instanceof PackScreen || mc.currentScreen instanceof AccessibilityOptionsScreen) rpc.setState("Đang thay đổi cài đặt");
                    else if (mc.currentScreen instanceof CreditsScreen) rpc.setState("Đang đọc credit");
                    else if (mc.currentScreen instanceof RealmsScreen) rpc.setState("Đang tìm kiếm Realms");
                    else if (mc.currentScreen instanceof ChatScreen) rpc.setState("Đang trò chuyện");
                    else if (mc.currentScreen instanceof InventoryScreen) rpc.setState("Đang mở túi đồ");
                    else {
                        boolean setState = false;
                        if (mc.currentScreen != null) {
                            String className = mc.currentScreen.getClass().getName();
                            for (var pair : customStates) {
                                if (className.startsWith(pair.getLeft())) {
                                    String state = pair.getRight();
                                    state = new String(state.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                                    rpc.setState(state);
                                    setState = true;
                                    break;
                                }
                            }
                        }
                        if (!setState) rpc.setState("Đang quay tay giữa màn đêm");
                    }

                    update = true;
                }
            }

            // Update
            if (update) DiscordIPC.setActivity(rpc);
            forceUpdate = false;
            lastWasInMainMenu = !Utils.canUpdate();
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) lastWasInMainMenu = false;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Xem hướng dẫn..");
        help.action = () -> Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");

        return help;
    }

    private enum SmallImage {
        hongminh54("hongminh54", "hongminh54"),
        sourcecode54("sourcecode54", "sourcecode54");

        private final String key, text;

        SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            rpc.setSmallImage(key, text);
        }

        SmallImage next() {
            if (this == hongminh54) return sourcecode54;
            return hongminh54;
        }
    }
}
