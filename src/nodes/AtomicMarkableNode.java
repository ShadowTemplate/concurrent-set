package nodes;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableNode<E extends Comparable<? super E>> extends Node<E> {

    public AtomicMarkableReference<AtomicMarkableNode<E>> next = new AtomicMarkableReference<>(null, false);

    public AtomicMarkableNode(E item) {
        super(item);
    }
}
