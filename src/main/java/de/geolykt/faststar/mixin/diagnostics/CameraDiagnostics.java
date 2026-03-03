package de.geolykt.faststar.mixin.diagnostics;

import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.graphics.OrthographicCamera;

import de.geolykt.starloader.DebugNagException;

@Mixin(OrthographicCamera.class)
public class CameraDiagnostics {

    @Unique
    private transient boolean touched;

    @Unique
    private transient ThreadLocal<Boolean> isOwnedThread;

    @Unique
    private transient Throwable initialTouchCause;

    @Inject(at = @At("HEAD"), method = "update(Z)V")
    private void faststar$onUpdate(CallbackInfo ci) {
        if (!this.touched) {
            this.isOwnedThread = new ThreadLocal<>();
            this.initialTouchCause = new DebugNagException().fillInStackTrace();
            this.touched = true;
            this.isOwnedThread.set(true);
        } else if (this.isOwnedThread != null) {
            Boolean bool = this.isOwnedThread.get();

            if (bool == null || bool == false) {
                LoggerFactory.getLogger(CameraDiagnostics.class).warn("FastStar/CameraDiagnostics: Camera object shared between threads first used by following:", this.initialTouchCause);
                LoggerFactory.getLogger(CameraDiagnostics.class).warn("FastStar/CameraDiagnostics: Camera object shared between threads now used by following: ", new DebugNagException().fillInStackTrace());

                this.isOwnedThread.set(true); // Let's not engage in logspam
            }
        }
    }
}
