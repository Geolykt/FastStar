package de.geolykt.faststar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.faststar.intrinsics.LandmarkPopulator;
import de.geolykt.faststar.intrinsics.LandmarkPopulator.Landmark;

import snoddasmannen.galimulator.Star;
import snoddasmannen.galimulator.guides.AStarGuide;
import snoddasmannen.galimulator.guides.LandmarkManager;

@Mixin(AStarGuide.class)
public class AStarGuideMixins {
    @Shadow
    private boolean useLandmarks;

    @Overwrite
    public float getRemainingEstimate(Star star0, Star star1) {
        if (this.useLandmarks && LandmarkManager.hasLandmarksGenerated()) {
            Landmark l0 = LandmarkPopulator.CLOSEST_LANDMARKS.get(star0.getId());
            Landmark l1 = LandmarkPopulator.CLOSEST_LANDMARKS.get(star1.getId());
            float distS0L0 = l0.getDistances().get((de.geolykt.starloader.api.empire.Star) star0).floatValue();
            float distS1L1 = l1.getDistances().get((de.geolykt.starloader.api.empire.Star) star1).floatValue();
            float distL0L1 = l0.getCenter().getDistance(l1.getCenter());
            return distS0L0 + distS1L1 + distL0L1;
        } else {
            LandmarkPopulator.CLOSEST_LANDMARKS.clear();
            return (float)Star.distanceBetween(star0, star1);
        }
    }

}
