package meteordevelopment.meteorvnclient.utils.render.postprocess;

import meteordevelopment.meteorvnclient.systems.modules.Modules;
import meteordevelopment.meteorvnclient.systems.modules.render.StorageESP;
import net.minecraft.entity.Entity;

public class StorageOutlineShader extends PostProcessShader {
    private static StorageESP storageESP;

    public StorageOutlineShader() {
        init("outline");
    }

    @Override
    protected void preDraw() {
        framebuffer.clear();
        framebuffer.beginWrite(false);
    }

    @Override
    protected boolean shouldDraw() {
        if (storageESP == null) storageESP = Modules.get().get(StorageESP.class);
        return storageESP.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        return true;
    }

    @Override
    protected void setUniforms() {
        shader.set("u_Width", storageESP.outlineWidth.get());
        shader.set("u_FillOpacity", storageESP.fillOpacity.get() / 255.0);
        shader.set("u_ShapeMode", storageESP.shapeMode.get().ordinal());
        shader.set("u_GlowMultiplier", storageESP.glowMultiplier.get());
    }
}
