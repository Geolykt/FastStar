package de.geolykt.faststar.intrinsics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpatialQueryArray<E> {
    public static class PointObjectPair<T> implements Comparable<PointObjectPair<?>> {
        @NotNull
        private final T object;
        private final float x;
        private final float y;

        public PointObjectPair(@NotNull T object, float x, float y) {
            this.object = object;
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(PointObjectPair<?> o) {
            return Float.compare(this.x, o.x);
        }
    }

    private final PointObjectPair<E>[] points;

    public SpatialQueryArray(@NotNull Collection<PointObjectPair<E>> points) {
        this.points = points.toArray(new PointObjectPair[0]);
        Arrays.sort(this.points);
    }

    private int binarySearch(int leftAnchor, int rightAnchor, float x) {
        rightAnchor--;

        while (leftAnchor <= rightAnchor) {
            final int center = (leftAnchor + rightAnchor) / 2;
            final int compareResult = Float.compare(this.points[center].x, x);
            if (compareResult < 0) {
                leftAnchor = center + 1;
            } else if (compareResult == 0) {
                return center;
            } else {
                rightAnchor = center - 1;
            }
        }

        return leftAnchor;
    }

    @Nullable
    public E query1nn(float x, float y, float minDistSq, float maxDistSq) {
        PointObjectPair<E> pair = this.query1nn0(x, y, minDistSq, maxDistSq);
        return pair == null ? null : pair.object;
    }

    @Nullable
    private PointObjectPair<E> query1nn0(float x, float y, float minDistSq, float maxDistSq) {
        int maxPoints = this.points.length;
        int searchOrigin = this.binarySearch(0, maxPoints, x);

        int leftEdge = searchOrigin - 1;
        int rightEdge = searchOrigin;

        PointObjectPair<E> currentNearest = null;
        while (true) {
            if (leftEdge >= 0) {
                PointObjectPair<E> pair = this.points[leftEdge--];
                float dx = pair.x - x;
                dx *= dx;
                if (dx > maxDistSq) {
                    if (rightEdge >= maxPoints) {
                        break;
                    }
                    leftEdge = -1;
                } else {
                    float dy = pair.y - y;
                    float distSq = dx + dy * dy;
                    if (distSq >= minDistSq && distSq < maxDistSq) {
                        currentNearest = pair;
                        maxDistSq = distSq;
                    }
                }
            }
            if (rightEdge < maxPoints) {
                PointObjectPair<E> pair = this.points[rightEdge++];
                float dx = pair.x - x;
                dx *= dx;
                if (dx > maxDistSq) {
                    if (leftEdge < 0) {
                        break;
                    }
                    rightEdge = maxPoints;
                } else {
                    float dy = pair.y - y;
                    float distSq = dx + dy * dy;
                    if (distSq >= minDistSq && distSq < maxDistSq) {
                        currentNearest = pair;
                        maxDistSq = distSq;
                    }
                }
            } else if (leftEdge < 0) {
                break; // Emergency break
            }
        }

        return currentNearest;
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
                    PointObjectPair<E> element = SpatialQueryArray.this.query1nn0(x, y, this.minDistance, Float.POSITIVE_INFINITY);
                    if (element == null) {
                        this.minDistance = Float.POSITIVE_INFINITY;
                        return false;
                    } else {
                        this.nextElement = element.object;
                        float dx = element.x - x;
                        float dy = element.y - y;
                        this.minDistance = Math.nextUp(dx * dx + dy * dy); // FIXME That is this line is especially bad, but that's a little bit mandated here
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

    public void queryKnn(float x, float y, int nearestNeighbours, Collection<E> out) {
        // FIXME this algorithm is inappropriate if multiple objects have the same distance
        float minDistance = 0F;
        while (nearestNeighbours-- != 0) {
            @Nullable
            PointObjectPair<E> element = this.query1nn0(x, y, minDistance, Float.POSITIVE_INFINITY);
            if (element == null) {
                return;
            } else {
                out.add(element.object);
                float dx = element.x - x;
                float dy = element.y - y;
                minDistance = Math.nextUp(dx * dx + dy * dy); // That is this line is especially bad, but that's a little bit mandated here
            }
        }
    }
}
