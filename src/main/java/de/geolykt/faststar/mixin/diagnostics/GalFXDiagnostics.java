package de.geolykt.faststar.mixin.diagnostics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.graphics.OrthographicCamera;

import de.geolykt.faststar.diagnostics.CameraDiagnosticsExtensions;

import snoddasmannen.galimulator.GalFX;

@Mixin(GalFX.class)
public class GalFXDiagnostics {
    @Shadow
    private static OrthographicCamera widgetToScreenCamera;

    @Inject(at = @At("TAIL"), target = @Desc("centerCamera"))
    private static void onCenterCamera(CallbackInfo ci) {
        ((CameraDiagnosticsExtensions) GalFXDiagnostics.widgetToScreenCamera).faststar$setImmutable(true);
    }
}
