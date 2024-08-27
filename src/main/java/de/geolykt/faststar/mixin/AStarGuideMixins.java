package de.geolykt.faststar.mixin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.faststar.FastStar;
import de.geolykt.faststar.intrinsics.LandmarkPopulator;
import de.geolykt.faststar.intrinsics.LandmarkPopulator.Landmark;
import de.geolykt.starloader.api.Galimulator;

import snoddasmannen.galimulator.Space;
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
                List<?> reachable0 = ((de.geolykt.starloader.api.empire.Star) star0).getNeighboursRecursive(Integer.MAX_VALUE);
                List<?> reachable1 = ((de.geolykt.starloader.api.empire.Star) star1).getNeighboursRecursive(Integer.MAX_VALUE);
                LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.values().forEach(landmarks::add);

                LoggerFactory.getLogger(FastStar.class).warn("AStarGuideMixins: No route to star: Unable to obtain landmark for star:"
                        + "s0: {}, s1: {}; l0: {}; l1: {}; Star count (authorative): {}; Star count (landmarks): {};"
                        + "Reachable stars0: {}; Reachable stars1: {}; Reachable s0 -> s1: {}; Reachable s1 -> s0: {}."
                        + "Landmark count: {}. This error is likely caused by disconnected networks."
                        + "Reportedly used connection method: {}.",
                        star0.getId(), star1.getId(), l0, l1, Galimulator.getUniverse().getStarsView().size(),
                        LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.size, reachable0.size(), reachable1.size(),
                        reachable0.contains(star1), reachable1.contains(star0), landmarks.size(),
                        Space.getMapData().getConnectionMethod());

                return Float.POSITIVE_INFINITY;
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
