package de.geolykt.faststar.intrinsics;

import java.util.AbstractList;

public class NOPList<E> extends AbstractList<E> {
    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public void add(int index, E element) {
        // NOP
    }

    @Override
    public E get(int index) {
        throw new IndexOutOfBoundsException(index + " out of bounds for a list of size 0.");
    }

    @Override
    public int size() {
        return 0;
    }
}
