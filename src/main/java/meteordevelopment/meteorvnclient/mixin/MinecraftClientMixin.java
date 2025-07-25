/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorvnclient.events.game.GameLeftEvent;
import meteordevelopment.meteorvnclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorvnclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorvnclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorvnclient.events.world.TickEvent;
import meteordevelopment.meteorvnclient.gui.WidgetScreen;
import meteordevelopment.meteorvnclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorvnclient.systems.config.Config;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.player.FastUse;
import meteordevelopment.meteorvnclient.systems.modules.player.Multitask;
import meteordevelopment.meteorvnclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.CPSUtils;
import meteordevelopment.meteorvnclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorvnclient.utils.network.OnlinePlayers;
import meteordevelopment.starscript.Script;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftClient.class, priority = 1001)
public abstract class MinecraftClientMixin implements IMinecraftClient {
    @Unique private boolean doItemUseCalled;
    @Unique private boolean rightClick;
    @Unique private long lastTime;
    @Unique private boolean firstFrame;

    @Shadow public ClientWorld world;
    @Shadow @Final public Mouse mouse;
    @Shadow @Final private Window window;
    @Shadow public Screen currentScreen;
    @Shadow @Final public GameOptions options;

    @Shadow protected abstract void doItemUse();

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    private int itemUseCooldown;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        MeteorVNClient.INSTANCE.onInitializeClient();
        firstFrame = true;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        OnlinePlayers.update();

        doItemUseCalled = false;

        Profilers.get().push(MeteorVNClient.MOD_ID + "_pre_update");
        MeteorVNClient.EVENT_BUS.post(TickEvent.Pre.get());
        Profilers.get().pop();

        if (rightClick && !doItemUseCalled && interactionManager != null) doItemUse();
        rightClick = false;
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        Profilers.get().push(MeteorVNClient.MOD_ID + "_post_update");
        MeteorVNClient.EVENT_BUS.post(TickEvent.Post.get());
        Profilers.get().pop();
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        CPSUtils.onAttack();
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUse(CallbackInfo info) {
        doItemUseCalled = true;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, boolean transferring, CallbackInfo info) {
        if (world != null) {
            MeteorVNClient.EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen) screen.mouseMoved(mouse.getX() * window.getScaleFactor(), mouse.getY() * window.getScaleFactor());

        OpenScreenEvent event = OpenScreenEvent.get(screen);
        MeteorVNClient.EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private void onDoItemUseHand(CallbackInfo ci, @Local ItemStack itemStack) {
        FastUse fastUse = Modules.get().get(FastUse.class);
        if (fastUse.isActive()) {
            itemUseCooldown = fastUse.getItemUseCooldown(itemStack);
        }
    }

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult doItemUseMinecraftClientCrosshairTargetProxy(HitResult original) {
        return MeteorVNClient.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get(original)).target;
    }

    @ModifyReturnValue(method = "reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<Void> onReloadResourcesNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> MeteorVNClient.EVENT_BUS.post(ResourcePacksReloadedEvent.get()));
    }

    @ModifyArg(method = "updateWindowTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        if (Config.get() == null || !Config.get().customWindowTitle.get()) return original;

        String customTitle = Config.get().customWindowTitleText.get();
        Script script = MeteorStarscript.compile(customTitle);

        if (script != null) {
            String title = MeteorStarscript.run(script);
            if (title != null) customTitle = title;
        }

        return customTitle;
    }

    // Have to add this condition if we want to draw back a bow using packets, without it getting cancelled by vanilla code
    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private boolean wrapStopUsing(ClientPlayerInteractionManager instance, PlayerEntity player) {
        return HB$stopUsingItem();
    }

    @Unique
    private boolean HB$stopUsingItem() {
        HighwayBuilder b = Modules.get().get(HighwayBuilder.class);
        return !b.isActive() || !b.drawingBow;
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo info) {
        MeteorVNClient.EVENT_BUS.post(ResolutionChangedEvent.get());
    }

    // Time delta

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();

        if (firstFrame) {
            lastTime = time;
            firstFrame = false;
        }

        Utils.frameTime = (time - lastTime) / 1000.0;
        lastTime = time;
    }

    // multitask

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean doItemUseModifyIsBreakingBlock(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean handleBlockBreakingModifyIsUsingItem(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0))
    private boolean handleInputEventsModifyIsUsingItem(boolean original) {
        return !Modules.get().get(Multitask.class).attackingEntities() && original;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void handleInputEventsInjectStopUsingItem(CallbackInfo info) {
        if (Modules.get().get(Multitask.class).attackingEntities() && player.isUsingItem()) {
            if (!options.useKey.isPressed() && HB$stopUsingItem()) interactionManager.stopUsingItem(player);
            while (options.useKey.wasPressed());
        }
    }

    // Interface

    @Override
    public void meteor$rightClick() {
        rightClick = true;
    }
}
