package de.geolykt.faststar;

import java.util.Collection;
import java.util.List;

import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.QuadTreePair;

public class QuadTreePairAssembler {
    public static interface QuadTree {
        List<Star> fastgalgen$getStarsFast();
        float fastgalgen$getContainerWidth();
        QuadTree fastgalgen$getQuadrantA();
        QuadTree fastgalgen$getQuadrantB();
        QuadTree fastgalgen$getQuadrantC();
        QuadTree fastgalgen$getQuadrantD();
        Star fastgalgen$getNearestStarTo(QuadTree other);
    }

    public static void assemblePairs(QuadTree rootQuad, QuadTree neighbourQuad, Collection<QuadTreePair> out) {
        if (rootQuad.fastgalgen$getContainerWidth() < neighbourQuad.fastgalgen$getContainerWidth()) {
            QuadTree swapCache = rootQuad;
            rootQuad = neighbourQuad;
            neighbourQuad = swapCache;
        }

        List<Star> parentStars = rootQuad.fastgalgen$getStarsFast();
        List<Star> childStars = neighbourQuad.fastgalgen$getStarsFast();

        if (parentStars.isEmpty() || childStars.isEmpty()) {
            return; // Nothing to do there I believe
        }

        double containerWidthSq = rootQuad.fastgalgen$getContainerWidth();
        containerWidthSq *= containerWidthSq;

        if (containerWidthSq < 2 * rootQuad.fastgalgen$getNearestStarTo(neighbourQuad).getDistanceSquared(neighbourQuad.fastgalgen$getNearestStarTo(rootQuad))) {
            out.add(new QuadTreePair(((snoddasmannen.galimulator.QuadTree) rootQuad).getStoredStars(), ((snoddasmannen.galimulator.QuadTree) neighbourQuad).getStoredStars()));
        } else {
            if (rootQuad.fastgalgen$getQuadrantA() == null) {
                return;
            }
            QuadTreePairAssembler.assemblePairs(rootQuad.fastgalgen$getQuadrantA(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.fastgalgen$getQuadrantB(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.fastgalgen$getQuadrantC(), neighbourQuad, out);
            QuadTreePairAssembler.assemblePairs(rootQuad.fastgalgen$getQuadrantD(), neighbourQuad, out);
        }
    }
}
