/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.modules.render;


import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorvnclient.events.game.GameLeftEvent;
import meteordevelopment.meteorvnclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorvnclient.events.meteor.KeyEvent;
import meteordevelopment.meteorvnclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorvnclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorvnclient.events.packets.PacketEvent;
import meteordevelopment.meteorvnclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorvnclient.events.world.TickEvent;
import meteordevelopment.meteorvnclient.settings.BoolSetting;
import meteordevelopment.meteorvnclient.settings.DoubleSetting;
import meteordevelopment.meteorvnclient.settings.Setting;
import meteordevelopment.meteorvnclient.settings.SettingGroup;
import meteordevelopment.meteorvnclient.systems.modules.Categories;
import meteordevelopment.meteorvnclient.systems.modules.Module;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorvnclient.utils.Utils;
import meteordevelopment.meteorvnclient.utils.misc.input.Input;
import meteordevelopment.meteorvnclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorvnclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Your speed while in freecam.")
        .onChanged(aDouble -> speedValue = aDouble)
        .defaultValue(1.0)
        .min(0.0)
        .build()
    );

    private final Setting<Double> speedScrollSensitivity = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed-scroll-sensitivity")
        .description("Allows you to change speed value using scroll wheel. 0 to disable.")
        .defaultValue(0)
        .min(0)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> toggleOnDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-damage")
        .description("Disables freecam when you take damage.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-death")
        .description("Disables freecam when you die.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables freecam when you disconnect from a server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> reloadChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("reload-chunks")
        .description("Disables cave culling.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderHands = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hands")
        .description("Whether or not to render your hands in freecam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to the block or entity you are looking at.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> staticView = sgGeneral.add(new BoolSetting.Builder()
        .name("static")
        .description("Disables settings that move the view.")
        .defaultValue(true)
        .build()
    );

    public final Vector3d pos = new Vector3d();
    public final Vector3d prevPos = new Vector3d();

    private Perspective perspective;
    private double speedValue;

    public float yaw, pitch;
    public float prevYaw, prevPitch;

    private double fovScale;
    private boolean bobView;

    private boolean forward, backward, right, left, up, down;

    public Freecam() {
        super(Categories.Render, "freecam", "Allows the camera to move away from the player.");
    }

    @Override
    public void onActivate() {
        fovScale = mc.options.getFovEffectScale().getValue();
        bobView = mc.options.getBobView().getValue();
        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue((double)0);
            mc.options.getBobView().setValue(false);
        }
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();
        speedValue = speed.get();

        Utils.set(pos, mc.gameRenderer.getCamera().getPos());
        Utils.set(prevPos, mc.gameRenderer.getCamera().getPos());

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        prevYaw = yaw;
        prevPitch = pitch;

        forward = mc.options.forwardKey.isPressed();
        backward = mc.options.backKey.isPressed();
        right = mc.options.rightKey.isPressed();
        left = mc.options.leftKey.isPressed();
        up = mc.options.jumpKey.isPressed();
        down = mc.options.sneakKey.isPressed();

        unpress();
        if (reloadChunks.get()) mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        if (reloadChunks.get()) {
            if (!RenderSystem.isOnRenderThread()) RenderSystem.recordRenderCall(mc.worldRenderer::reload);
            else mc.worldRenderer.reload();
        }
        mc.options.setPerspective(perspective);
        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(bobView);
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        unpress();

        prevPos.set(pos);
        prevYaw = yaw;
        prevPitch = pitch;
    }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.cameraEntity.isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velY = 0;
        double velZ = 0;

        if (rotate.get()) {
            BlockPos crossHairPos;
            Vec3d crossHairPosition;

            if (mc.crosshairTarget instanceof EntityHitResult) {
                crossHairPos = ((EntityHitResult) mc.crosshairTarget).getEntity().getBlockPos();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                crossHairPosition = mc.crosshairTarget.getPos();
                crossHairPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

                if (!mc.world.getBlockState(crossHairPos).isAir()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }

        double s = 0.5;
        if (mc.options.sprintKey.isPressed()) s = 1;

        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * speedValue;
            velZ += forward.z * s * speedValue;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * speedValue;
            velZ -= forward.z * s * speedValue;
            a = true;
        }

        boolean b = false;
        if (this.right) {
            velX += right.x * s * speedValue;
            velZ += right.z * s * speedValue;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * speedValue;
            velZ -= right.z * s * speedValue;
            b = true;
        }

        if (a && b) {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }

        if (this.up) {
            velY += s * speedValue;
        }
        if (this.down) {
            velY -= s * speedValue;
        }

        prevPos.set(pos);
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return;
        if (checkGuiMove()) return;

        boolean cancel = true;

        if (mc.options.forwardKey.matchesKey(event.key, 0)) {
            forward = event.action != KeyAction.Release;
            mc.options.forwardKey.setPressed(false);
        }
        else if (mc.options.backKey.matchesKey(event.key, 0)) {
            backward = event.action != KeyAction.Release;
            mc.options.backKey.setPressed(false);
        }
        else if (mc.options.rightKey.matchesKey(event.key, 0)) {
            right = event.action != KeyAction.Release;
            mc.options.rightKey.setPressed(false);
        }
        else if (mc.options.leftKey.matchesKey(event.key, 0)) {
            left = event.action != KeyAction.Release;
            mc.options.leftKey.setPressed(false);
        }
        else if (mc.options.jumpKey.matchesKey(event.key, 0)) {
            up = event.action != KeyAction.Release;
            mc.options.jumpKey.setPressed(false);
        }
        else if (mc.options.sneakKey.matchesKey(event.key, 0)) {
            down = event.action != KeyAction.Release;
            mc.options.sneakKey.setPressed(false);
        }
        else {
            cancel = false;
        }

        if (cancel) event.cancel();
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (checkGuiMove()) return;

        boolean cancel = true;

        if (mc.options.forwardKey.matchesMouse(event.button)) {
            forward = event.action != KeyAction.Release;
            mc.options.forwardKey.setPressed(false);
        }
        else if (mc.options.backKey.matchesMouse(event.button)) {
            backward = event.action != KeyAction.Release;
            mc.options.backKey.setPressed(false);
        }
        else if (mc.options.rightKey.matchesMouse(event.button)) {
            right = event.action != KeyAction.Release;
            mc.options.rightKey.setPressed(false);
        }
        else if (mc.options.leftKey.matchesMouse(event.button)) {
            left = event.action != KeyAction.Release;
            mc.options.leftKey.setPressed(false);
        }
        else if (mc.options.jumpKey.matchesMouse(event.button)) {
            up = event.action != KeyAction.Release;
            mc.options.jumpKey.setPressed(false);
        }
        else if (mc.options.sneakKey.matchesMouse(event.button)) {
            down = event.action != KeyAction.Release;
            mc.options.sneakKey.setPressed(false);
        }
        else {
            cancel = false;
        }

        if (cancel) event.cancel();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onMouseScroll(MouseScrollEvent event) {
        if (speedScrollSensitivity.get() > 0 && mc.currentScreen == null) {
            speedValue += event.value * 0.25 * (speedScrollSensitivity.get() * speedValue);
            if (speedValue < 0.1) speedValue = 0.1;

            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;

        toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event)  {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.playerId());
            if (entity == mc.player && toggleOnDeath.get()) {
                toggle();
                info("Toggled off because you died.");
            }
        }
        else if (event.packet instanceof HealthUpdateS2CPacket packet) {
            if (mc.player.getHealth() - packet.getHealth() > 0 && toggleOnDamage.get()) {
                toggle();
                info("Toggled off because you took damage.");
            }
        }
    }

    private boolean checkGuiMove() {
        // TODO: This is very bad but you all can cope :cope:
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        if (mc.currentScreen != null && !guiMove.isActive()) return true;
        return (mc.currentScreen != null && guiMove.isActive() && guiMove.skip());
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        prevYaw = yaw;
        prevPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public boolean renderHands() {
        return !isActive() || renderHands.get();
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }
    public double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }
    public double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public double getYaw(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevYaw, yaw);
    }
    public double getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPitch, pitch);
    }
}
