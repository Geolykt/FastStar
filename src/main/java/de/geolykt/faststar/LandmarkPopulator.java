package de.geolykt.faststar;

import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.badlogic.gdx.utils.IntSet;

import de.geolykt.starloader.api.empire.Star;

public class LandmarkPopulator {
    public static interface Landmark {
        public Star getCenter();
        public Map<Star, Float> getDistances();
    }

    public static void populateLandmark(Landmark landmark) {
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
    }
}
