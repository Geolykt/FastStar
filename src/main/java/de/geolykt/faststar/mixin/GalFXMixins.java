package de.geolykt.faststar.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import snoddasmannen.galimulator.GalColor;
import snoddasmannen.galimulator.GalFX;

@Mixin(GalFX.class)
public class GalFXMixins {
    @Redirect(
        target = @Desc(value = "drawTexture", args = {TextureRegion.class, double.class, double.class, double.class, double.class, double.class, GalColor.class, boolean.class, Camera.class}),
        at = @At(value = "INVOKE", desc = @Desc(owner = SpriteBatch.class, value = "setProjectionMatrix", args = Matrix4.class)),
        require = 1,
        allow = 1
    )
    private static void faststar$drawTexture$smartSetProjectionMatrix(SpriteBatch reciever, Matrix4 projectionMatrix) {
        // Avoid unnecessary SpriteBatch flushes (and thus unnecessary draw calls)
        if (!Arrays.equals(reciever.getProjectionMatrix().val, projectionMatrix.val)) {
            reciever.setProjectionMatrix(projectionMatrix);
        }
    }

    @Redirect(
        target = @Desc(value = "drawVertices", args = float[].class),
        at = @At(value = "INVOKE", desc = @Desc(owner = SpriteBatch.class, value = "setProjectionMatrix", args = Matrix4.class)),
        require = 1,
        allow = 1
    )
    private static void faststar$drawVertices$smartSetProjectionMatrix(SpriteBatch reciever, Matrix4 projectionMatrix) {
        // Avoid unnecessary SpriteBatch flushes (and thus unnecessary draw calls)
        if (!Arrays.equals(reciever.getProjectionMatrix().val, projectionMatrix.val)) {
            reciever.setProjectionMatrix(projectionMatrix);
        }
    }
}
