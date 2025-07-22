package meteordevelopment.meteorvnclient.utils.render.postprocess;

import meteordevelopment.meteorvnclient.mixininterface.IWorldRenderer;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public abstract class EntityShader extends PostProcessShader {
    @Override
    protected void preDraw() {
        ((IWorldRenderer) mc.worldRenderer).meteor$pushEntityOutlineFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        ((IWorldRenderer) mc.worldRenderer).meteor$popEntityOutlineFramebuffer();
    }

    public void endRender() {
        endRender(() -> vertexConsumerProvider.draw());
    }
}
