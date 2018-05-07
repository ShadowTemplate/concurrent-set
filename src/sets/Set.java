package sets;

public interface Set<E extends Comparable<? super E>> {

    boolean add(E item);
    boolean remove(E item);
    boolean contains(E item);

    int getSize();
}
