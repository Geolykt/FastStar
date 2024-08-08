package de.geolykt.faststar.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Cancellable;

import de.geolykt.faststar.AsynchronousAuxiliaryPanListener;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.utils.TickLoopLock.LockScope;

import snoddasmannen.galimulator.AuxiliaryListener;
import snoddasmannen.galimulator.GalimulatorGestureListener;
import snoddasmannen.galimulator.Space;

@Mixin(GalimulatorGestureListener.class)
public class GalimulatorGestureListenerMixins {
    @Redirect(method = "pan",
            at = @At(value = "INVOKE", desc = @Desc(owner = java.util.Vector.class, value = "isEmpty", ret = boolean.class)),
            slice = @Slice(
                from = @At(value = "INVOKE", desc = @Desc(owner = Space.class, value = "getAuxiliaryListeners", ret = java.util.Vector.class)),
                to = @At(value = "INVOKE", desc = @Desc(owner = AuxiliaryListener.class, value = "globalPan", args = {float.class, float.class}, ret = boolean.class))
            )
    )
    private boolean faststar$handleAuxiliaryPanListeners(java.util.Vector<AuxiliaryListener> listeners,
            float deltaX, float deltaY, @NotNull @Cancellable CallbackInfoReturnable<Boolean> cir) {
        // FIXME this is a compatibility nightmare; use a bunch of @WrapOperations instead once that is supported by micromixin.
        LockScope acquiredScope = null;

        try {
            for (AuxiliaryListener listener : listeners) {
                if (!(listener instanceof AsynchronousAuxiliaryPanListener) && acquiredScope == null) {
                    acquiredScope = Galimulator.getSimulationLoopLock().acquireHardControlWithResources();
                }

                if (listener.globalPan(deltaX, deltaY)) {
                    // Abort continued execution of parent method - this prevents further camera translations
                    cir.setReturnValue(true);
                    return true;
                }
            }
        } catch (InterruptedException e) {
            Galimulator.panic("Unable to acquire hard control of simulation loop: Unexpectedly interrupted", true, e);
        } finally {
            if (acquiredScope != null) {
                acquiredScope.close();
            }
        }

        return true; // Suppress vanilla auxiliary pan listening logic
    }
}
