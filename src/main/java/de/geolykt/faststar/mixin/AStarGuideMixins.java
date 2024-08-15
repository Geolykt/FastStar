package de.geolykt.faststar.mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.faststar.intrinsics.LandmarkPopulator;
import de.geolykt.faststar.intrinsics.LandmarkPopulator.Landmark;
import de.geolykt.starloader.api.Galimulator;

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
            if (LandmarkPopulator.LANDMARK_POPULATION_COUNTER.get() != 0) {
                throw new IllegalStateException("Landmark population != 0 (race condition)");
            }

            Landmark l0 = LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.get(star0.getId());
            Landmark l1 = LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.get(star1.getId());

            if (l0 == null || l1 == null) {
                Set<Landmark> landmarks = new HashSet<>();
                LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.values().forEach(landmarks::add);
                throw new IllegalStateException("Unable to obtain landmark for star: " + star0.getId() + ", " + star1.getId() + ";"
                        + "l0: " + l0 + "; l1: " + l1 + "; Star count: " + LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.size + ";"
                        + "Landmark count: " + landmarks.size());
            }

            float distS0L0 = l0.getDistances().get((de.geolykt.starloader.api.empire.Star) star0).floatValue();
            float distS1L1 = l1.getDistances().get((de.geolykt.starloader.api.empire.Star) star1).floatValue();
            float distL0L1 = l0.getCenter().getDistance(l1.getCenter());
            return distS0L0 + distS1L1 + distL0L1;
        } else {
            LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.clear();
            return (float)Star.distanceBetween(star0, star1);
        }
    }

}
