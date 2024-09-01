package de.geolykt.faststar.intrinsics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import de.geolykt.faststar.intrinsics.SpatialQueryArray.PointObjectPair;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;

public class SpatialQuery {

    private static SpatialQueryArray<Star> stars;
    private static SpatialQueryArray<Actor> actors;

    public static Actor getNearestActor(float x, float y, @NotNull Actor witness) {
        Iterator<Actor> it = SpatialQuery.actors.queryKnn(x, y);
        if (!it.hasNext()) {
            LoggerFactory.getLogger(SpatialQuery.class).warn("Actor {} not contained in SpatialQueryArray", witness);
            return null;
        }
        it.next();
        return it.hasNext() ? it.next() : null;
    }

    public static Actor getNearestActorHostile(float x, float y, @NotNull Actor witness) {
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

    public static void updateActorsActorTicking() {
        List<PointObjectPair<Actor>> actorPositions = new ArrayList<>();
        for (Actor a : Space.actors) {
            actorPositions.add(new PointObjectPair<>(a, a.getX(), a.getY()));
        }
        SpatialQuery.actors = new SpatialQueryArray<>(actorPositions);
    }

    public static void updateStarsActorDrawing() {
        List<PointObjectPair<Star>> starPositions = new ArrayList<>();
        for (Star s : Galimulator.getUniverse().getStarsView()) {
            starPositions.add(new PointObjectPair<>(s, s.getX(), s.getY()));
        }
        SpatialQuery.stars = new SpatialQueryArray<>(starPositions);
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
