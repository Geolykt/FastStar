package de.geolykt.faststar.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import snoddasmannen.galimulator.Job;

@Mixin(Job.class)
public class JobMixins {
    @Redirect(at = @At(value = "INVOKE", desc = @Desc(value = "removeHolder")), target = @Desc(value = "vacate", args = String.class), require = 1, allow = 1)
    private void faststar$noDuplicateRemoveHolder(@NotNull Job redirectArgThis) {
        // The vacate method already calls Person#kill, which in turns call Job#removeHolder via Person#endCVChapter.
        // However, afterwards the vacate method calls Job#removeHolder again, which is pointless
        // and causes undefined nullability issues aside from being a useless performance drain.

        // As a fix, we redirect the removeHolder method to a NOP.
    }
}
