package de.geolykt.faststar.mixin.diagnostics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.graphics.OrthographicCamera;

import de.geolykt.faststar.diagnostics.CameraDiagnosticsExtensions;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.gui.Drawing;

@Mixin(OrthographicCamera.class)
public class CameraDiagnostics implements CameraDiagnosticsExtensions {

    @Unique
    private transient boolean immutable;

    @Unique
    private transient boolean touched;

    @Unique
    private transient ThreadLocal<Boolean> isOwnedThread;

    @Override
    public void faststar$setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    @Inject(at = @At("HEAD"), method = "update(Z)V")
    private void faststar$onUpdate(CallbackInfo ci) {
        if (!this.touched) {
            this.isOwnedThread = new ThreadLocal<>();
            this.touched = true;
            this.isOwnedThread.set(true);
        } else if (this.isOwnedThread != null) {
            Boolean bool = this.isOwnedThread.get();

            if (bool == null || bool == false) {
                Galimulator.panic("Camera object shared between multiple threads!", true);
            }
        }

        if (this.immutable) {
            if (Drawing.isRenderThread()) { // Still allow single-threaded access
                return;
            }

            Galimulator.panic("An off-thread mutation attempt occured on an immutable camera object. This may cause issues with asynchronous use, so use a new camera object instead.", true);
        }
    }
}
