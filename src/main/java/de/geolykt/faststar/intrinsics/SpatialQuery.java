package de.geolykt.faststar.intrinsics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.stianloader.stianknn.PointObjectPair;
import org.stianloader.stianknn.SpatialIndexKNN;
import org.stianloader.stianknn.SpatialQueryArray;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;

public class SpatialQuery {

    private static SpatialIndexKNN<@NotNull Star> stars;
    @SuppressWarnings("deprecation")
    private static org.stianloader.stianknn.SpatialQueryArrayLegacy<@NotNull Actor> actors;

    public static Actor getNearestActor(float x, float y, @NotNull Actor witness) {
        @SuppressWarnings("deprecation")
        Iterator<Actor> it = SpatialQuery.actors.queryKnn(x, y);
        if (!it.hasNext()) {
            LoggerFactory.getLogger(SpatialQuery.class).warn("Actor {} not contained in SpatialQueryArray", witness);
            return null;
        }
        it.next();
        return it.hasNext() ? it.next() : null;
    }

    public static Actor getNearestActorHostile(float x, float y, @NotNull Actor witness) {
        @SuppressWarnings("deprecation")
        Iterator<Actor> it = SpatialQuery.actors.queryKnn(x, y);
        while (it.hasNext()) {
            Actor v = it.next();
            if (v == witness
                    || v.isSubordinateOf(witness)
                    || v.isUntouchable()
                    || (witness.getOwner() != null && !witness.isHostile(v))) {
                continue;
            }
            return v;
        }

        return null;
    }

    public static void getNearestStars(float x, float y, int cutoff, @NotNull List<Star> out) {
        SpatialQuery.stars.queryKnn(x, y, cutoff, out::add);
    }

    @SuppressWarnings("deprecation")
    public static void updateActorsActorTicking() {
        List<@NotNull PointObjectPair<@NotNull Actor>> actorPositions = new ArrayList<>();
        for (Actor a : Space.actors) {
            actorPositions.add(new PointObjectPair<>(a, a.getX(), a.getY()));
        }
        SpatialQuery.actors = new org.stianloader.stianknn.SpatialQueryArrayLegacy<>(actorPositions);
    }

    public static void updateStarsActorDrawing() {
        List<PointObjectPair<@NotNull Star>> starPositions = new ArrayList<>();
        for (Star s : Galimulator.getUniverse().getStarsView()) {
            starPositions.add(new PointObjectPair<>(s, s.getX(), s.getY()));
        }
        @SuppressWarnings("deprecation")
        float minX = Galimulator.getMap().getWidth() * -0.6F;
        float maxX = -minX;
        @SuppressWarnings("deprecation")
        float minY = Galimulator.getMap().getHeight() * -0.6F;
        float maxY = -minY;
        float gridW = 16 * 0.035F;
        float gridH = 16 * 0.035F;
        SpatialQuery.stars = new SpatialQueryArray<>(starPositions, minX, minY, maxX, maxY, gridW, gridH);
    }
    /*

    private static PointMap<Star> stars;
    private static PointMap<Actor> actors;

    public static Actor getNearestActor(double x, double y, @NotNull Actor witness) {
        PointIteratorKnn<Actor> it = SpatialQuery.actors.queryKnn(new double[] {x, y}, 2);
        it.next();
        return it.hasNext() ? it.next().value() : null;
    }

    public static Actor getNearestActorHostile(double x, double y, @NotNull Actor witness) {
        PointIteratorKnn<Actor> it = SpatialQuery.actors.queryKnn(new double[] {x, y}, Integer.MAX_VALUE);
        while (it.hasNext()) {
            Actor v = it.next().value();
            if (v == witness
                    || v.isSubordinateOf(witness)
                    || v.isUntouchable()
                    || (witness.getOwner() != null && !witness.isHostile(v))) {
                continue;
            }
            return v;
        }
        return null;
    }

    public static void getNearestStars(double x, double y, int cutoff, @NotNull List<Star> out) {
        PointIteratorKnn<Star> it = SpatialQuery.stars.queryKnn(new double[] {x, y}, cutoff);
        while (it.hasNext()) {
            out.add(it.next().value());
        }
    }

    public static void updateActorsActorTicking() {
        PointMap<Actor> actors = KDTree.create(2);
        for (Actor a : Space.actors) {
            actors.insert(new double[] {a.getX(), a.getY()}, a);
        }
        SpatialQuery.actors = actors;
    }

    public static void updateStarsActorDrawing() {
        PointMap<Star> stars = KDTree.create(2);
        for (Star s : Galimulator.getUniverse().getStarsView()) {
            stars.insert(new double[] {s.getX(), s.getY()}, s);
        }
        SpatialQuery.stars = stars;
    }*/
}
