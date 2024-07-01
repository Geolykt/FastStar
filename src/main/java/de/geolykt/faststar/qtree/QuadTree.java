package de.geolykt.faststar.qtree;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector2f;

public final class QuadTree<E> {
    private static <T> void addElement(QuadTreeLeaf<T> leaf, T element, float x, float y, @NotNull Function<T, Vector2f> locationDescriptor) {
        if (leaf.quadrantA == null && leaf.storedElement == null) {
            leaf.storedElement = element;
            return;
        }

        final float cx = leaf.x1 + (leaf.x2 - leaf.x1) / 2;
        final float cy = leaf.y1 + (leaf.y2 - leaf.y1) / 2;

        if (leaf.quadrantA == null) {
            leaf.quadrantA = new QuadTreeLeaf<>(cx, cy, leaf.x2, leaf.y2);
            leaf.quadrantB = new QuadTreeLeaf<>(cx, leaf.y1, leaf.x2, cy);
            leaf.quadrantC = new QuadTreeLeaf<>(leaf.x1, cy, cx, leaf.y2);
            leaf.quadrantD = new QuadTreeLeaf<>(leaf.x1, leaf.y1, cx, cy);

            @Nullable // since `T` could technically speaking correspond to a @NotNull data type, so we override that behaviour
            T oldElement = leaf.storedElement;
            assert oldElement != null; // The null check occurred previously already

            Vector2f oldElementLoc = locationDescriptor.apply(oldElement);
            float oldElementX = oldElementLoc.x;
            float oldElementY = oldElementLoc.y;

            QuadTreeLeaf<T> quadrant;
            if (oldElementX < cx) {
                if (oldElementY < cy) {
                    quadrant = leaf.quadrantD;
                } else {
                    quadrant = leaf.quadrantC;
                }
            } else if (oldElementY < cy) {
                quadrant = leaf.quadrantB;
            } else {
                quadrant = leaf.quadrantA;
            }

            assert quadrant != null; // Already populated earlier
            quadrant.storedElement = oldElement;
            leaf.storedElement = null;

            if (oldElementX == x && oldElementY == y) {
                return; // FIXME Really, really dirty hackfix! This will swallow quite a few things
            }
        }

        if (x < cx) {
            if (y < cy) {
                // Quadrant D
                QuadTree.addElement(leaf.quadrantD, element, x, y, locationDescriptor);
            } else {
                // Quadrant C
                QuadTree.addElement(leaf.quadrantC, element, x, y, locationDescriptor);
            }
        } else if (y < cy) {
            // Quadrant B
            QuadTree.addElement(leaf.quadrantB, element, x, y, locationDescriptor);
        } else {
            // Quadrant A
            QuadTree.addElement(leaf.quadrantA, element, x, y, locationDescriptor);
        }
    }

    @Nullable
    private static <T> T query1nnRecurse(@Nullable T defaultValue, float x, float y, float minDistSq,
            float @NotNull[] maxDistSq, @NotNull QuadTreeLeaf<T> leaf,
            @NotNull Function<T, Vector2f> locationDescriptor) {

        {
            @Nullable
            T element = leaf.storedElement;
            if ((element = leaf.storedElement) != null) {
                Vector2f location = locationDescriptor.apply(element);
                x -= location.x;
                y -= location.y;
                float distSq = x * x + y * y;
                if (distSq > minDistSq && distSq < maxDistSq[0]) {
                    maxDistSq[0] = distSq;
                    return element;
                } else {
                    return defaultValue;
                }
            } else if (leaf.quadrantA == null) {
                return defaultValue;
            }
        }

        float quadWidthHalf = (leaf.x2 - leaf.x1) / 2;
        float quadHeightHalf = (leaf.y2 - leaf.y1) / 2;
        float quadCenterDistX = Math.abs(leaf.x1 + quadWidthHalf - x);
        float quadCenterDistY = Math.abs(leaf.y1 + quadHeightHalf - y);
        float quadDistXMin = quadCenterDistX - quadWidthHalf;
        float quadDistYMin = quadCenterDistY - quadHeightHalf;
        float quadDistXMax = quadCenterDistX + quadWidthHalf;
        float quadDistYMax = quadCenterDistY + quadHeightHalf;

        float quadDistSqMin = quadDistXMin * quadDistXMin + quadDistYMin * quadDistYMin;
        float quadDistSqMax = quadDistXMax * quadDistXMax + quadDistYMax * quadDistYMax;

        if (quadDistSqMin < minDistSq || quadDistSqMax > maxDistSq[0]) {
            return defaultValue;
        }

        QuadTreeLeaf<T> quadA = leaf.quadrantA;
        QuadTreeLeaf<T> quadB = leaf.quadrantB;
        QuadTreeLeaf<T> quadC = leaf.quadrantC;
        QuadTreeLeaf<T> quadD = leaf.quadrantD;
        assert quadA != null; // Check already performed prior
        assert quadB != null; // All other quadrants are non-null if one quad is non-null
        assert quadC != null; // All other quadrants are non-null if one quad is non-null
        assert quadD != null; // All other quadrants are non-null if one quad is non-null

        defaultValue = QuadTree.query1nnRecurse(defaultValue, x, y, minDistSq, maxDistSq, quadA, locationDescriptor);
        defaultValue = QuadTree.query1nnRecurse(defaultValue, x, y, minDistSq, maxDistSq, quadB, locationDescriptor);
        defaultValue = QuadTree.query1nnRecurse(defaultValue, x, y, minDistSq, maxDistSq, quadC, locationDescriptor);
        defaultValue = QuadTree.query1nnRecurse(defaultValue, x, y, minDistSq, maxDistSq, quadD, locationDescriptor);

        return defaultValue;
    }

    @NotNull
    private final Function<E, Vector2f> locationDescriptor;
    @NotNull
    private QuadTreeLeaf<E> rootLeafA;
    @NotNull
    private QuadTreeLeaf<E> rootLeafB;
    @NotNull
    private QuadTreeLeaf<E> rootLeafC;
    @NotNull
    private QuadTreeLeaf<E> rootLeafD;

    public QuadTree(float initialRadius, @NotNull Function<E, Vector2f> locationDescriptor) {
        this.rootLeafA = new QuadTreeLeaf<>(0, 0, initialRadius, initialRadius);
        this.rootLeafB = new QuadTreeLeaf<>(0, -initialRadius, initialRadius, 0);
        this.rootLeafC = new QuadTreeLeaf<>(-initialRadius, 0, 0, initialRadius);
        this.rootLeafD = new QuadTreeLeaf<>(-initialRadius, -initialRadius, 0, 0);
        this.locationDescriptor = locationDescriptor;
    }

    public void addElement(@NotNull E element) {
        final float x, y;
        {
            final Vector2f v = this.locationDescriptor.apply(element);
            x = v.x;
            y = v.y;
        }

        if (x < 0) {
            if (y < 0) {
                // Quadrant D
                QuadTreeLeaf<E> leaf = this.rootLeafD;
                while (x < leaf.x1 || y < leaf.y1) {
                    QuadTreeLeaf<E> parent = new QuadTreeLeaf<>(leaf.x1 * 2, leaf.y1 * 2, 0, 0);
                    parent.quadrantA = leaf;
                    parent.quadrantB = new QuadTreeLeaf<>(leaf.x1, parent.y1, 0, leaf.y1);
                    parent.quadrantC = new QuadTreeLeaf<>(parent.x1, leaf.y1, leaf.x1, 0);
                    parent.quadrantD = new QuadTreeLeaf<>(parent.x1, parent.y1, leaf.x1, leaf.y1);
                    this.rootLeafD = leaf = parent;
                }

                QuadTree.addElement(leaf, element, x, y, this.locationDescriptor);
            } else {
                // Quadrant C
                QuadTreeLeaf<E> leaf = this.rootLeafC;
                while (x < leaf.x1 || y > leaf.y2) {
                    QuadTreeLeaf<E> parent = new QuadTreeLeaf<>(leaf.x1 * 2, 0, 0, leaf.y2 * 2);
                    parent.quadrantA = new QuadTreeLeaf<>(leaf.x1, leaf.y2, 0, parent.y2);
                    parent.quadrantB = leaf;
                    parent.quadrantC = new QuadTreeLeaf<>(parent.x1, leaf.y2, leaf.x1, parent.y2);
                    parent.quadrantD = new QuadTreeLeaf<>(parent.x1, 0, leaf.x1, leaf.y2);
                    this.rootLeafC = leaf = parent;
                }

                QuadTree.addElement(leaf, element, x, y, this.locationDescriptor);
            }
        } else if (y < 0) {
            // Quadrant B
            QuadTreeLeaf<E> leaf = this.rootLeafB;
            while (x > leaf.x2 || y < leaf.y1) {
                QuadTreeLeaf<E> parent = new QuadTreeLeaf<>(0, leaf.y1 * 2, leaf.x2 * 2, 0);
                parent.quadrantA = new QuadTreeLeaf<>(leaf.x2, leaf.y1, parent.x2, 0);
                parent.quadrantB = new QuadTreeLeaf<>(leaf.x2, parent.y1, parent.x2, leaf.y1);
                parent.quadrantC = leaf;
                parent.quadrantD = new QuadTreeLeaf<>(0, parent.y1, leaf.x2, leaf.y1);
                this.rootLeafB = leaf = parent;
            }

            QuadTree.addElement(leaf, element, x, y, this.locationDescriptor);
        } else {
            // Quadrant A
            QuadTreeLeaf<E> leaf = this.rootLeafA;
            while (x > leaf.x2 || y < leaf.y1) {
                QuadTreeLeaf<E> parent = new QuadTreeLeaf<>(0, 0, leaf.x2 * 2, leaf.y2 * 2);
                parent.quadrantA = new QuadTreeLeaf<>(leaf.x2, leaf.y2, parent.x2, parent.y2);
                parent.quadrantB = new QuadTreeLeaf<>(leaf.x2, 0, parent.x2, leaf.y2);
                parent.quadrantC = new QuadTreeLeaf<>(0, leaf.y2, leaf.x2, parent.y2);
                parent.quadrantD = leaf;
                this.rootLeafA = leaf = parent;
            }

            QuadTree.addElement(leaf, element, x, y, this.locationDescriptor);
        }
    }

    @Nullable
    public E query1nn(float x, float y, float minDistSq, float maxDistSq) {
        return this.query1nn(x, y, minDistSq, new float[] { maxDistSq });
    }

    @Nullable
    private E query1nn(float x, float y, float minDistSq, float @NotNull[] maxDistSq) {
        @Nullable
        E nearest = QuadTree.query1nnRecurse(null, x, y, minDistSq, maxDistSq, this.rootLeafA, this.locationDescriptor);
        nearest = QuadTree.query1nnRecurse(nearest, x, y, minDistSq, maxDistSq, this.rootLeafB, this.locationDescriptor);
        nearest = QuadTree.query1nnRecurse(nearest, x, y, minDistSq, maxDistSq, this.rootLeafC, this.locationDescriptor);
        nearest = QuadTree.query1nnRecurse(nearest, x, y, minDistSq, maxDistSq, this.rootLeafD, this.locationDescriptor);

        return nearest;
    }

    public void queryKnn(float x, float y, int nearestNeighbours, Collection<E> out) {
        // FIXME this algorithm is inappropriate if multiple objects have the same distance
        float minDistance = 0F;
        while (nearestNeighbours-- != 0) {
            @Nullable
            E element = this.query1nn(x, y, minDistance, Float.POSITIVE_INFINITY);
            if (element == null) {
                return;
            } else {
                out.add(element);
                Vector2f pos = this.locationDescriptor.apply(element);
                float dx = pos.x - x;
                float dy = pos.y - y;
                minDistance = dx * dx + dy * dy;
            }
        }
    }

    @NotNull
    public Iterator<@NotNull E> queryKnn(float x, float y) {
        return new Iterator<@NotNull E>() {
            private float minDistance = 0F;
            @Nullable
            private E nextElement;

            @Override
            public boolean hasNext() {
                if (this.nextElement == null) {
                    if (this.minDistance == Float.POSITIVE_INFINITY) {
                        return false;
                    }
                    @Nullable
                    E element = QuadTree.this.query1nn(x, y, this.minDistance, Float.POSITIVE_INFINITY);
                    if (element == null) {
                        this.minDistance = Float.POSITIVE_INFINITY;
                        return false;
                    } else {
                        this.nextElement = element;
                        Vector2f pos = QuadTree.this.locationDescriptor.apply(element);
                        float dx = pos.x - x;
                        float dy = pos.y - y;
                        this.minDistance = dx * dx + dy * dy;
                    }
                }
                return true;
            }

            @Override
            @NotNull
            public E next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("Iterator exhausted");
                }
                @Nullable
                E element = this.nextElement;
                assert element != null; // This check is done via #hasNext()
                this.nextElement = null;
                return element;
            }
        };
    }
}
