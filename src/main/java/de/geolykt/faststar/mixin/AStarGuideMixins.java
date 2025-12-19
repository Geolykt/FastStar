package de.geolykt.faststar.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.faststar.intrinsics.LandmarkPopulator;

import snoddasmannen.galimulator.Star;
import snoddasmannen.galimulator.guides.AStarGuide;
import snoddasmannen.galimulator.guides.LandmarkManager;

@Mixin(AStarGuide.class)
public class AStarGuideMixins {
    @Shadow
    private boolean useLandmarks;

    @Overwrite
    public float getRemainingEstimate(@NotNull Star star0, @NotNull Star star1) {
        if (this.useLandmarks && LandmarkManager.hasLandmarksGenerated()) {
            de.geolykt.starloader.api.empire.Star s0 = (de.geolykt.starloader.api.empire.Star) star0;
            de.geolykt.starloader.api.empire.Star s1 = (de.geolykt.starloader.api.empire.Star) star1;
            return LandmarkPopulator.getRemainingEstimate(s0, s1);
        } else {
            LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.clear();
            return (float)Star.distanceBetween(star0, star1);
        }
    }
}
