package de.geolykt.faststar.qtree;

import org.jetbrains.annotations.Nullable;

final class QuadTreeLeaf<T> {

    @Nullable
    protected QuadTreeLeaf<T> quadrantA;
    @Nullable
    protected QuadTreeLeaf<T> quadrantB;
    @Nullable
    protected QuadTreeLeaf<T> quadrantC;
    @Nullable
    protected QuadTreeLeaf<T> quadrantD;

    protected final float x1;
    protected final float x2;
    protected final float y1;
    protected final float y2;
    @Nullable
    protected T storedElement;

    public QuadTreeLeaf(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
