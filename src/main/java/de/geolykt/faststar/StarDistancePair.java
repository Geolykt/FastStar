package de.geolykt.faststar;

import de.geolykt.starloader.api.empire.Star;

public class StarDistancePair implements Comparable<StarDistancePair> {
    public final Star star;
    // Note: Due to how this distance in accumulative, you can't plainly use distanceSq
    public final float distance;

    public StarDistancePair(Star star, float distance) {
        this.star = star;
        this.distance = distance;
    }

    @Override
    public int compareTo(StarDistancePair o) {
        int delta = Float.compare(this.distance, o.distance);
        if (delta == 0) {
            return this.star.getUID() - o.star.getUID();
        }
        return delta;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StarDistancePair)) {
            return false;
        }
        return ((StarDistancePair) obj).star.getUID() == this.star.getUID()
                && ((StarDistancePair) obj).distance == this.distance;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(this.distance) ^ this.star.getUID() * 31;
    }
}
