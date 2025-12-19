package de.geolykt.faststar.intrinsics;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;

import de.geolykt.faststar.FastStar;
import de.geolykt.faststar.StarDistancePair;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.Space;

public class LandmarkPopulator {
    public static interface Landmark {
        public Star getCenter();
        public Map<Star, Float> getDistances();
    }

    public static final Map<Integer, Landmark> CLOSEST_LANDMARKS_CONCURRENT = new ConcurrentHashMap<>();
    public static final IntMap<Landmark> CLOSEST_LANDMARKS_SYNCHRONIZED = new IntMap<>();
    public static final AtomicInteger LANDMARK_POPULATION_COUNTER = new AtomicInteger();

    public static float getRemainingEstimate(@NotNull Star star0, @NotNull Star star1) {
        if (LandmarkPopulator.LANDMARK_POPULATION_COUNTER.get() != 0) {
            throw new IllegalStateException("Landmark population != 0 (race condition)");
        }

        Landmark l0 = LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.get(star0.getUID());
        Landmark l1 = LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.get(star1.getUID());

        if (l0 == null || l1 == null) {
            Set<Landmark> landmarks = new HashSet<>();
            List<?> reachable0 = star0.getNeighboursRecursive(Integer.MAX_VALUE);
            List<?> reachable1 = star1.getNeighboursRecursive(Integer.MAX_VALUE);
            LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.values().forEach(landmarks::add);

            LoggerFactory.getLogger(FastStar.class).warn("AStarGuideMixins: No route to star: Unable to obtain landmark for star:"
                    + "s0: {}, s1: {}; l0: {}; l1: {}; Star count (authorative): {}; Star count (landmarks): {};"
                    + "Reachable stars0: {}; Reachable stars1: {}; Reachable s0 -> s1: {}; Reachable s1 -> s0: {}."
                    + "Landmark count: {}. This error is likely caused by disconnected networks."
                    + "Reportedly used connection method: {}.",
                    star0.getUID(), star1.getUID(), l0, l1, Galimulator.getUniverse().getStarsView().size(),
                    LandmarkPopulator.CLOSEST_LANDMARKS_SYNCHRONIZED.size, reachable0.size(), reachable1.size(),
                    reachable0.contains(star1), reachable1.contains(star0), landmarks.size(),
                    Space.getMapData().getConnectionMethod());

            return Float.POSITIVE_INFINITY;
        }

        float distS0L0 = l0.getDistances().get(star0).floatValue();
        float distS1L1 = l1.getDistances().get(star1).floatValue();
        float distL0L1 = l0.getCenter().getDistance(l1.getCenter());
        return distS0L0 + distS1L1 + distL0L1;
    }

    public static void populateLandmark(Landmark landmark) {
        LandmarkPopulator.LANDMARK_POPULATION_COUNTER.incrementAndGet();
        IntSet bfsVisitedStarIds = new IntSet();
        Map<Star, Float> distances = landmark.getDistances();
        NavigableSet<StarDistancePair> scheduledVisits = new TreeSet<>();

        for (Star neighbour : landmark.getCenter().getNeighbourList()) {
            float dist = neighbour.getDistance(landmark.getCenter());
            scheduledVisits.add(new StarDistancePair(neighbour, dist));
            distances.put(neighbour, dist);
        }

        while (!scheduledVisits.isEmpty()) {
            StarDistancePair pair = scheduledVisits.pollFirst();
            bfsVisitedStarIds.add(pair.star.getUID());

            for (Star neighbour : pair.star.getNeighbourList()) {
                if (bfsVisitedStarIds.contains(neighbour.getUID())) {
                    continue;
                }
                float neighbourDist = neighbour.getDistance(pair.star) +  pair.distance;
                Float oldDist = distances.get(neighbour);
                if (oldDist != null) {
                    float oldDistF = oldDist.floatValue();
                    if (neighbourDist < oldDistF) {
                        continue;
                    }
                    scheduledVisits.remove(new StarDistancePair(neighbour, oldDistF));
                }
                scheduledVisits.add(new StarDistancePair(neighbour, neighbourDist));
                distances.put(neighbour, neighbourDist);
            }
        }

        for (Map.Entry<Star, Float> entry : distances.entrySet()) {
            LandmarkPopulator.CLOSEST_LANDMARKS_CONCURRENT.compute(entry.getKey().getUID(), (uid, currentLandmark) -> {
                if (currentLandmark == null) {
                    return landmark;
                }
                Float distance = currentLandmark.getDistances().get(entry.getKey());
                return (distance != null && distance.floatValue() < entry.getValue()) ? currentLandmark : landmark;
            });
        }

        LandmarkPopulator.LANDMARK_POPULATION_COUNTER.decrementAndGet();
    }
}
