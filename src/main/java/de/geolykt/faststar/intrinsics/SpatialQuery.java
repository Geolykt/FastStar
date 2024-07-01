package de.geolykt.faststar.intrinsics;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;
import org.slf4j.LoggerFactory;

import de.geolykt.faststar.qtree.QuadTree;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.Star;

import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.actors.Actor;

public class SpatialQuery {

    private static QuadTree<Star> stars;
    private static QuadTree<Actor> actors;
    private static final Vector2f VECTOR_TEMP_DRAW = new Vector2f();
    private static final Vector2f VECTOR_TEMP_TICK = new Vector2f();

    public static Actor getNearestActor(float x, float y, @NotNull Actor witness) {
        Iterator<Actor> it = SpatialQuery.actors.queryKnn(x, y);
        if (!it.hasNext()) {
            LoggerFactory.getLogger(SpatialQuery.class).warn("Actor {} not contained in QuadTree", witness);
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
        SpatialQuery.stars.queryKnn(x, y, cutoff, out);
    }

    public static void updateActorsActorTicking() {
        SpatialQuery.actors = new QuadTree<>(Space.getMaxX(), (actor) -> {
            SpatialQuery.VECTOR_TEMP_TICK.set(actor.getX(), actor.getY());
            return SpatialQuery.VECTOR_TEMP_TICK;
        });
        for (Actor a : Space.actors) {
            assert a != null;
            SpatialQuery.actors.addElement(a);
        }
    }

    public static void updateStarsActorDrawing() {
        SpatialQuery.stars = new QuadTree<>(Space.getMaxX(), (star) -> {
            SpatialQuery.VECTOR_TEMP_DRAW.set(star.getX(), star.getY());
            return SpatialQuery.VECTOR_TEMP_DRAW;
        });
        for (Star star : Galimulator.getUniverse().getStarsView()) {
            SpatialQuery.stars.addElement(star);
        }
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
