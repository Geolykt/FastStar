package de.geolykt.faststar.intrinsics;

import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;

import de.geolykt.faststar.StarDistancePair;
import de.geolykt.starloader.api.empire.Star;

public class LandmarkPopulator {
    public static interface Landmark {
        public Star getCenter();
        public Map<Star, Float> getDistances();
    }

    public static final IntMap<Landmark> CLOSEST_LANDMARKS = new IntMap<>();

    public static void populateLandmark(Landmark landmark) {
        LandmarkPopulator.CLOSEST_LANDMARKS.clear();
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
            Landmark otherLM = LandmarkPopulator.CLOSEST_LANDMARKS.get(entry.getKey().getUID());
            if (otherLM != null && otherLM.getDistances().get(entry.getKey()) < entry.getValue()) {
                continue;
            }
            LandmarkPopulator.CLOSEST_LANDMARKS.put(entry.getKey().getUID(), landmark);
        }
    }
}
