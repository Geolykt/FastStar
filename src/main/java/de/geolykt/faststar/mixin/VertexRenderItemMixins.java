package de.geolykt.faststar.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import snoddasmannen.galimulator.GalFX;
import snoddasmannen.galimulator.rendersystem.VertexRenderItem;

@Mixin(VertexRenderItem.class)
public class VertexRenderItemMixins {
    @Shadow
    private float[] vertices;

    // FIXME Something seems to be wrong with micromixin-transformer here.
    /*
    @Inject(
        // Sadly micromixin doesn't yet support @Inject targeting non-return instructions.
        //at = @At(value = "INVOKE", desc = @Desc(owner = RenderItem.class, value = "<init>")),
        at = @At(value = "TAIL"),
        target = @Desc(value = "<init>", args = float[].class),
        require = 1,
        allow = 1
    )
    private void faststar$assertVerticesNonNull(@NotNull CallbackInfo ci, float[] vertices) {
        Objects.requireNonNull(vertices);
    }
    */

    private void faststar$intrinsicRenderVertexItem() {
        SpriteBatch batch = GalFX.mainSpriteBatch;
        Matrix4 projMat = GalFX.getBoardCamera().combined;

        if (!Arrays.equals(batch.getProjectionMatrix().val, projMat.val)) {
            batch.setProjectionMatrix(projMat);
        }

        batch.draw(GalFX.whitesquare.getTexture(), this.vertices, 0, 20);
    }

    @Overwrite
    public void render() {
        this.faststar$intrinsicRenderVertexItem();
    }
}
