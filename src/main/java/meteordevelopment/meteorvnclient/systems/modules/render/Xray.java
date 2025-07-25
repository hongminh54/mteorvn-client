/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.render;

import meteordevelopment.meteorvnclient.MixinPlugin;
import meteordevelopment.meteorvnclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorvnclient.events.world.AmbientOcclusionEvent;
import meteordevelopment.meteorvnclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.widgets.WWidget;
import meteordevelopment.meteorvnclient.settings.*;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;

import java.util.List;

public class Xray extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public static final List<Block> ORES = List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS);

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Which blocks to show x-rayed.")
        .defaultValue(ORES)
        .onChanged(v -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    public final Setting<Integer> opacity = sgGeneral.add(new IntSetting.Builder()
        .name("opacity")
        .description("The opacity for all other blocks.")
        .defaultValue(25)
        .range(0, 255)
        .sliderMax(255)
        .onChanged(onChanged -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    private final Setting<Boolean> exposedOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("exposed-only")
        .description("Show only exposed ores.")
        .defaultValue(false)
        .onChanged(onChanged -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build());

    public Xray() {
        super(Categories.Render, "xray", "Only renders specified blocks. Good for mining.");
    }

    @Override
    public void onActivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isSodiumPresent) return theme.label("Warning: Due to Sodium in use, opacity is overridden to 0.");
        if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) return theme.label("Warning: Due to shaders in use, opacity is overridden to 0.");

        return null;
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (isBlocked(event.blockEntity.getCachedState().getBlock(), event.blockEntity.getPos())) event.cancel();
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1;
    }

    public boolean modifyDrawSide(BlockState state, BlockState otherState, Direction facing, boolean returns) {
        if (!returns && !isBlocked(state, otherState)) {
            return otherState.getCullingFace(facing.getOpposite()) != VoxelShapes.fullCube() || otherState.getBlock() != state.getBlock() || !otherState.isOpaque();
        }

        return returns;
    }

    public boolean isBlocked(BlockState state, BlockState otherState) {
        return !(blocks.get().contains(state.getBlock()) && (!exposedOnly.get() || !otherState.isOpaque()));
    }

    public boolean isBlocked(Block block, BlockPos blockPos) {
        return !(blocks.get().contains(block) && (!exposedOnly.get() || (blockPos == null || BlockUtils.isExposed(blockPos))));
    }

    public static int getAlpha(BlockState state, BlockPos pos) {
        WallHack wallHack = Modules.get().get(WallHack.class);
        Xray xray = Modules.get().get(Xray.class);

        if (wallHack.isActive() && wallHack.blocks.get().contains(state.getBlock())) {
            if (MixinPlugin.isSodiumPresent || (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse())) return 0;

            int alpha;

            if (xray.isActive()) alpha = xray.opacity.get();
            else alpha = wallHack.opacity.get();

            return alpha;
        }
        else if (xray.isActive() && !wallHack.isActive() && xray.isBlocked(state.getBlock(), pos)) {
            return (MixinPlugin.isSodiumPresent || (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse())) ? 0 : xray.opacity.get();
        }

        return -1;
    }
}
