package de.geolykt.faststar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import snoddasmannen.galimulator.GalFX;

@Mixin(GalFX.class)
public class GalFXMixins {
    @Redirect(target = @Desc(value = "a", args = float[].class), at = @At(value = "INVOKE", desc = @Desc(owner = SpriteBatch.class, value = "setProjectionMatrix", args = Matrix4.class)))
    private static void faststar$drawVertices$noSetProjectionMatrix(SpriteBatch reciever, Matrix4 projectionMatrix) {
        // Don't set the projection matrix for this method call as when this method is called repeatedly, severe performance
        // penalties arise.
    }
}
