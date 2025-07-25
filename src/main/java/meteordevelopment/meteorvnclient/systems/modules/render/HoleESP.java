/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.render;

import meteordevelopment.meteorvnclient.events.render.Render3DEvent;
import meteordevelopment.meteorvnclient.events.world.TickEvent;
import meteordevelopment.meteorvnclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorvnclient.renderer.Renderer3D;
import meteordevelopment.meteorvnclient.renderer.ShapeMode;
import meteordevelopment.meteorvnclient.settings.*;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.utils.misc.Pool;
import meteordevelopment.meteorvnclient.utils.render.color.Color;
import meteordevelopment.meteorvnclient.utils.render.color.SettingColor;
import meteordevelopment.meteorvnclient.utils.world.BlockIterator;
import meteordevelopment.meteorvnclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("horizontal-radius")
        .description("Horizontal radius in which to search for holes.")
        .defaultValue(10)
        .min(0)
        .sliderMax(32)
        .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("vertical-radius")
        .description("Vertical radius in which to search for holes.")
        .defaultValue(5)
        .min(0)
        .sliderMax(32)
        .build()
    );

    private final Setting<Integer> holeHeight = sgGeneral.add(new IntSetting.Builder()
        .name("min-height")
        .description("Minimum hole height required to be rendered.")
        .defaultValue(3)
        .min(1)
        .sliderMin(1)
        .build()
    );

    private final Setting<Boolean> doubles = sgGeneral.add(new BoolSetting.Builder()
        .name("doubles")
        .description("Highlights double holes that can be stood across.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreOwn = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-own")
        .description("Ignores rendering the hole you are currently standing in.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> webs = sgGeneral.add(new BoolSetting.Builder()
        .name("webs")
        .description("Whether to show holes that have webs inside of them.")
        .defaultValue(false)
        .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<Double> height = sgRender.add(new DoubleSetting.Builder()
        .name("height")
        .description("The height of rendering.")
        .defaultValue(0.2)
        .min(0)
        .build()
    );

    private final Setting<Boolean> topQuad = sgRender.add(new BoolSetting.Builder()
        .name("top-quad")
        .description("Whether to render a quad at the top of the hole.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> bottomQuad = sgRender.add(new BoolSetting.Builder()
        .name("bottom-quad")
        .description("Whether to render a quad at the bottom of the hole.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> bedrockColorTop = sgRender.add(new ColorSetting.Builder()
        .name("bedrock-top")
        .description("The top color for holes that are completely bedrock.")
        .defaultValue(new SettingColor(100, 255, 0, 200))
        .build()
    );

    private final Setting<SettingColor> bedrockColorBottom = sgRender.add(new ColorSetting.Builder()
        .name("bedrock-bottom")
        .description("The bottom color for holes that are completely bedrock.")
        .defaultValue(new SettingColor(100, 255, 0, 0))
        .build()
    );

    private final Setting<SettingColor> obsidianColorTop = sgRender.add(new ColorSetting.Builder()
        .name("obsidian-top")
        .description("The top color for holes that are completely obsidian.")
        .defaultValue(new SettingColor(255, 0, 0, 200))
        .build()
    );

    private final Setting<SettingColor> obsidianColorBottom = sgRender.add(new ColorSetting.Builder()
        .name("obsidian-bottom")
        .description("The bottom color for holes that are completely obsidian.")
        .defaultValue(new SettingColor(255, 0, 0, 0))
        .build()
    );

    private final Setting<SettingColor> mixedColorTop = sgRender.add(new ColorSetting.Builder()
        .name("mixed-top")
        .description("The top color for holes that have mixed bedrock and obsidian.")
        .defaultValue(new SettingColor(255, 127, 0, 200))
        .build()
    );

    private final Setting<SettingColor> mixedColorBottom = sgRender.add(new ColorSetting.Builder()
        .name("mixed-bottom")
        .description("The bottom color for holes that have mixed bedrock and obsidian.")
        .defaultValue(new SettingColor(255, 127, 0, 0))
        .build()
    );

    private final Pool<Hole> holePool = new Pool<>(Hole::new);
    private final List<Hole> holes = new ArrayList<>();

    private final byte NULL = 0;

    public HoleESP() {
        super(Categories.Render, "hole-esp", "Displays holes that you will take less damage in.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Hole hole : holes) holePool.free(hole);
        holes.clear();

        BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
            if (!validHole(blockPos)) return;

            int bedrock = 0, obsidian = 0;
            Direction air = null;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;
                BlockPos offsetPos = blockPos.offset(direction);
                BlockState state = mc.world.getBlockState(offsetPos);

                if (state.getBlock() == Blocks.BEDROCK) bedrock++;
                else if (state.getBlock() == Blocks.OBSIDIAN) obsidian++;
                else if (direction == Direction.DOWN) return;
                else if (doubles.get() && air == null && validHole(offsetPos)) {
                    for (Direction dir : Direction.values()) {
                        if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                        BlockState blockState1 = mc.world.getBlockState(offsetPos.offset(dir));

                        if (blockState1.getBlock() == Blocks.BEDROCK) bedrock++;
                        else if (blockState1.getBlock() == Blocks.OBSIDIAN) obsidian++;
                        else return;
                    }

                    air = direction;
                }
            }

            if (obsidian + bedrock == 5 && air == null) {
                holes.add(holePool.get().set(blockPos, obsidian == 5 ? Hole.Type.Obsidian : (bedrock == 5 ? Hole.Type.Bedrock : Hole.Type.Mixed), NULL));
            }
            else if (obsidian + bedrock == 8 && doubles.get() && air != null) {
                holes.add(holePool.get().set(blockPos, obsidian == 8 ? Hole.Type.Obsidian : (bedrock == 8 ? Hole.Type.Bedrock : Hole.Type.Mixed), Dir.get(air)));
            }
        });
    }

    private boolean validHole(BlockPos pos) {
        if (ignoreOwn.get() && mc.player.getBlockPos().equals(pos)) return false;

        WorldChunk chunk = mc.world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
        Block block = chunk.getBlockState(pos).getBlock();
        if (!webs.get() && block == Blocks.COBWEB) return false;

        if (((AbstractBlockAccessor) block).isCollidable()) return false;

        for (int i = 0; i < holeHeight.get(); i++) {
            if (((AbstractBlockAccessor) chunk.getBlockState(pos.up(i)).getBlock()).isCollidable()) return false;
        }

        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (HoleESP.Hole hole : holes) hole.render(event.renderer, shapeMode.get(), height.get(), topQuad.get(), bottomQuad.get());
    }

    private static class Hole {
        public BlockPos.Mutable blockPos = new BlockPos.Mutable();
        public byte exclude;
        public Type type;

        public Hole set(BlockPos blockPos, Type type, byte exclude) {
            this.blockPos.set(blockPos);
            this.exclude = exclude;
            this.type = type;

            return this;
        }

        public Color getTopColor() {
            return switch (this.type) {
                case Obsidian -> Modules.get().get(HoleESP.class).obsidianColorTop.get();
                case Bedrock  -> Modules.get().get(HoleESP.class).bedrockColorTop.get();
                default       -> Modules.get().get(HoleESP.class).mixedColorTop.get();
            };
        }

        public Color getBottomColor() {
            return switch (this.type) {
                case Obsidian -> Modules.get().get(HoleESP.class).obsidianColorBottom.get();
                case Bedrock  -> Modules.get().get(HoleESP.class).bedrockColorBottom.get();
                default       -> Modules.get().get(HoleESP.class).mixedColorBottom.get();
            };
        }

        public void render(Renderer3D renderer, ShapeMode mode, double height, boolean topQuad, boolean bottomQuad) {
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            Color top = getTopColor();
            Color bottom = getBottomColor();

            int originalTopA = top.a;
            int originalBottompA = bottom.a;

            if (mode.lines()) {
                if (Dir.isNot(exclude, Dir.WEST) && Dir.isNot(exclude, Dir.NORTH)) renderer.line(x, y, z, x, y + height, z, bottom, top);
                if (Dir.isNot(exclude, Dir.WEST) && Dir.isNot(exclude, Dir.SOUTH)) renderer.line(x, y, z + 1, x, y + height, z + 1, bottom, top);
                if (Dir.isNot(exclude, Dir.EAST) && Dir.isNot(exclude, Dir.NORTH)) renderer.line(x + 1, y, z, x + 1, y + height, z, bottom, top);
                if (Dir.isNot(exclude, Dir.EAST) && Dir.isNot(exclude, Dir.SOUTH)) renderer.line(x + 1, y, z + 1, x + 1, y + height, z + 1, bottom, top);

                if (Dir.isNot(exclude, Dir.NORTH)) renderer.line(x, y, z, x + 1, y, z, bottom);
                if (Dir.isNot(exclude, Dir.NORTH)) renderer.line(x, y + height, z, x + 1, y + height, z, top);
                if (Dir.isNot(exclude, Dir.SOUTH)) renderer.line(x, y, z + 1, x + 1, y, z + 1, bottom);
                if (Dir.isNot(exclude, Dir.SOUTH)) renderer.line(x, y + height, z + 1, x + 1, y + height, z + 1, top);

                if (Dir.isNot(exclude, Dir.WEST)) renderer.line(x, y, z, x, y, z + 1, bottom);
                if (Dir.isNot(exclude, Dir.WEST)) renderer.line(x, y + height, z, x, y + height, z + 1, top);
                if (Dir.isNot(exclude, Dir.EAST)) renderer.line(x + 1, y, z, x + 1, y, z + 1, bottom);
                if (Dir.isNot(exclude, Dir.EAST)) renderer.line(x + 1, y + height, z, x + 1, y + height, z + 1, top);
            }

            if (mode.sides()) {
                top.a = originalTopA / 2;
                bottom.a = originalBottompA / 2;

                if (Dir.isNot(exclude, Dir.UP) && topQuad) renderer.quad(x, y + height, z, x, y + height, z + 1, x + 1, y + height, z + 1, x + 1, y + height, z, top); // Top
                if (Dir.isNot(exclude, Dir.DOWN) && bottomQuad) renderer.quad(x, y, z, x, y, z + 1, x + 1, y, z + 1, x + 1, y, z, bottom); // Bottom

                if (Dir.isNot(exclude, Dir.NORTH)) renderer.gradientQuadVertical(x, y, z, x + 1, y + height, z, top, bottom); // North
                if (Dir.isNot(exclude, Dir.SOUTH)) renderer.gradientQuadVertical(x, y, z + 1, x + 1, y + height, z + 1, top, bottom); // South

                if (Dir.isNot(exclude, Dir.WEST)) renderer.gradientQuadVertical(x, y, z, x, y + height, z + 1, top, bottom); // West
                if (Dir.isNot(exclude, Dir.EAST)) renderer.gradientQuadVertical(x + 1, y, z, x + 1, y + height, z + 1, top, bottom); // East

                top.a = originalTopA;
                bottom.a = originalBottompA;
            }
        }

        public enum Type {
            Bedrock,
            Obsidian,
            Mixed
        }
    }
}
