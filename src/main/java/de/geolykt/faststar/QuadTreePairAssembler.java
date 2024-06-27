package de.geolykt.faststar;

import java.util.Collection;
import java.util.List;

import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.QuadTreePair;

public class QuadTreePairAssembler {
    public static interface QuadTree {
        List<Star> faststar$getStarsFast();
        float faststar$getContainerWidth();
        QuadTree faststar$getQuadrantA();
        QuadTree faststar$getQuadrantB();
        QuadTree faststar$getQuadrantC();
        QuadTree faststar$getQuadrantD();
        Star faststar$getNearestStarTo(QuadTree other);
    }

    public static void assemblePairs(QuadTree rootQuad, QuadTree neighbourQuad, Collection<QuadTreePair> out) {
        if (rootQuad.faststar$getContainerWidth() < neighbourQuad.faststar$getContainerWidth()) {
            QuadTree swapCache = rootQuad;
            rootQuad = neighbourQuad;
            neighbourQuad = swapCache;
        }

        List<Star> parentStars = rootQuad.faststar$getStarsFast();
        List<Star> childStars = neighbourQuad.faststar$getStarsFast();

        if (parentStars.isEmpty() || childStars.isEmpty()) {
            return; // Nothing to do there I believe
        }

        double containerWidthSq = rootQuad.faststar$getContainerWidth();
        containerWidthSq *= containerWidthSq;

        if (containerWidthSq < 2 * rootQuad.faststar$getNearestStarTo(neighbourQuad).getDistanceSquared(neighbourQuad.faststar$getNearestStarTo(rootQuad))) {
            out.add(new QuadTreePair(((snoddasmannen.galimulator.QuadTree) rootQuad).getStoredStars(), ((snoddasmannen.galimulator.QuadTree) neighbourQuad).getStoredStars()));
        } else {
            if (rootQuad.faststar$getQuadrantA() == null) {
                return;
            }
            QuadTreePairAssembler.assemblePairs(rootQuad.faststar$getQuadrantA(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.faststar$getQuadrantB(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.faststar$getQuadrantC(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.faststar$getQuadrantD(), neighbourQuad, out);
        }
    }
}
