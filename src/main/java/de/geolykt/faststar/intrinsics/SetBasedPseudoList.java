package de.geolykt.faststar.intrinsics;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

import org.jetbrains.annotations.NotNull;

public class SetBasedPseudoList<T> implements List<T>, Serializable {

    private static final long serialVersionUID = 2950270379643490259L;

    @NotNull
    private final Set<T> underlyingSet;

    public SetBasedPseudoList(@NotNull Set<T> underlyingSet) {
        this.underlyingSet = underlyingSet;
    }

    @Override
    public void add(int p0, T p1) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public boolean add(T p0) {
        return this.underlyingSet.add(p0);
    }

    @Override
    public boolean addAll(Collection<? extends T> p0) {
        return this.underlyingSet.addAll(p0);
    }

    @Override
    public boolean addAll(int p0, Collection<? extends T> p1) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public void clear() {
        this.underlyingSet.clear();
        return;
    }

    @Override
    public boolean contains(Object p0) {
        return this.underlyingSet.contains(p0);
    }

    @Override
    public boolean containsAll(Collection<?> p0) {
        return this.underlyingSet.containsAll(p0);
    }

    @Override
    public T get(int p0) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public int indexOf(Object p0) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public boolean isEmpty() {
        return this.underlyingSet.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return this.underlyingSet.iterator();
    }

    @Override
    public int lastIndexOf(Object p0) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public ListIterator<T> listIterator(int p0) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public T remove(int p0) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public boolean remove(Object p0) {
        return this.underlyingSet.remove(Objects.requireNonNull(p0, "cannot remove a null value"));
    }

    @Override
    public boolean removeAll(Collection<?> p0) {
        return this.underlyingSet.removeAll(p0);
    }

    @Override
    public boolean retainAll(Collection<?> p0) {
        return this.underlyingSet.retainAll(p0);
    }

    @Override
    public T set(int p0, T p1) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public int size() {
        return this.underlyingSet.size();
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.underlyingSet.spliterator();
    }

    @Override
    @NotNull
    public List<T> subList(int p0, int p1) {
        throw new UnsupportedOperationException("Index-based modification or access to this collection is forbidden.");
    }

    @Override
    public Object[] toArray() {
        return this.underlyingSet.toArray();
    }

    @Override
    public <E> E[] toArray(E[] p0) {
        return this.underlyingSet.toArray(p0);
    }
}
