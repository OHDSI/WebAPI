package org.ohdsi.webapi.cdmresults.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author ymolodkov
 */
public class SetWrapper<E> implements Set<E> {

    protected Set<E> values;

    protected SetWrapper(Set<E> values) {
        this.values = values;
    }

    @Override
    public boolean add(E e) {
        return values.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return values.addAll(collection);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return  values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return values.iterator();
    }

    @Override
    public Object[] toArray() {
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return values.toArray(ts);
    }

    @Override
    public boolean remove(Object o) {
        return values.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return values.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return values.removeAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return values.removeAll(collection);
    }

    @Override
    public void clear() {
        values.clear();
    }

}
