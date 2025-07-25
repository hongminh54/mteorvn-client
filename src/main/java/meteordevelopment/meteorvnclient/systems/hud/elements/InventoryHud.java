/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.hud.elements;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.settings.*;
import meteordevelopment.meteorvnclient.systems.hud.Hud;
import meteordevelopment.meteorvnclient.systems.hud.HudElement;
import meteordevelopment.meteorvnclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorvnclient.systems.hud.HudRenderer;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.render.color.Color;
import meteordevelopment.meteorvnclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class InventoryHud extends HudElement {
    public static final HudElementInfo<InventoryHud> INFO = new HudElementInfo<>(Hud.GROUP, "inventory", "Displays your inventory.", InventoryHud::new);

    private static final Identifier TEXTURE = MeteorVNClient.identifier("textures/container.png");
    private static final Identifier TEXTURE_TRANSPARENT = MeteorVNClient.identifier("textures/container-transparent.png");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> containers = sgGeneral.add(new BoolSetting.Builder()
        .name("containers")
        .description("Shows the contents of a container when holding them.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .onChanged(aDouble -> calculateSize())
        .build()
    );

    private final Setting<Background> background = sgGeneral.add(new EnumSetting.Builder<Background>()
        .name("background")
        .description("Background of inventory viewer.")
        .defaultValue(Background.Texture)
        .onChanged(bg -> calculateSize())
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> background.get() != Background.None)
        .build()
    );

    private final ItemStack[] containerItems = new ItemStack[9 * 3];

    private InventoryHud() {
        super(INFO);

        calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x, y = this.y;

        ItemStack container = getContainer();
        boolean hasContainer = containers.get() && container != null;
        if (hasContainer) Utils.getItemsInContainerItem(container, containerItems);
        Color drawColor = hasContainer ? Utils.getShulkerColor(container) : color.get();

        if (background.get() != Background.None) {
            drawBackground(renderer, (int) x, (int) y, drawColor);
        }

        if (mc.player == null) return;

        renderer.post(() -> {
            for (int row = 0; row < 3; row++) {
                for (int i = 0; i < 9; i++) {
                    int index = row * 9 + i;
                    ItemStack stack = hasContainer ? containerItems[index] : mc.player.getInventory().getStack(index + 9);
                    if (stack == null) continue;

                    int itemX = background.get() == Background.Texture ? (int) (x + (8 + i * 18) * scale.get()) : (int) (x + (1 + i * 18) * scale.get());
                    int itemY = background.get() == Background.Texture ? (int) (y + (7 + row * 18) * scale.get()) : (int) (y + (1 + row * 18) * scale.get());

                    renderer.item(stack, itemX, itemY, scale.get().floatValue(), true);
                }
            }
        });
    }

    private void calculateSize() {
        setSize(background.get().width * scale.get(), background.get().height * scale.get());
    }

    private void drawBackground(HudRenderer renderer, int x, int y, Color color) {
        int w = getWidth();
        int h = getHeight();

        switch (background.get()) {
            case Texture, Outline -> renderer.texture(background.get() == Background.Texture ? TEXTURE : TEXTURE_TRANSPARENT, x, y, w, h, color);
            case Flat -> renderer.quad(x, y, w, h, color);
        }
    }

    private ItemStack getContainer() {
        if (isInEditor() || mc.player == null) return null;

        ItemStack stack = mc.player.getOffHandStack();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) return stack;

        stack = mc.player.getMainHandStack();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) return stack;

        return null;
    }

    public enum Background {
        None(162, 54),
        Texture(176, 67),
        Outline(162, 54),
        Flat(162, 54);

        private final int width, height;

        Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
