/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.render.BetterTooltips;
import net.minecraft.component.type.DyedColorComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedColorComponent.class)
public abstract class DyedColorComponentMixin {
    @ModifyExpressionValue(method = "appendTooltip", at = @At(value = "FIELD", target = "Lnet/minecraft/component/type/DyedColorComponent;showInTooltip:Z"))
    private boolean modifyShowInTooltip(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return (bt.isActive() && bt.dye.get()) || original;
    }
}
