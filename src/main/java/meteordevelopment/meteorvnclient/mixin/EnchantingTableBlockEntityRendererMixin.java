/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.render.NoRender;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public abstract class EnchantingTableBlockEntityRendererMixin {
    @WrapWithCondition(method = "render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BookModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private boolean onRenderBookModelRenderProxy(BookModel instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay) {
        return !Modules.get().get(NoRender.class).noEnchTableBook();
    }
}
