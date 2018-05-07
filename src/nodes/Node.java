package nodes;

public class Node<E extends Comparable<? super E>> {

    public E item = null;
    public Node<E> next = null;

    public Node(E item) {
        this.item = item;
    }
}
