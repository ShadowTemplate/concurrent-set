package sets;

import nodes.Node;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;

public class CoarseGrainedListSet<E extends Comparable<? super E>> implements Set<E> {

    private final Lock lock = new ReentrantLock();
    private final Node<E> head;

    public CoarseGrainedListSet(E minValue, E maxValue) {
        head = new Node<>(minValue);
        head.next = new Node<>(maxValue);
    }

    private boolean executeSynchronized(E item, BiPredicate<Node<E>, Node<E>> onFound,
                                        BiPredicate<Node<E>, Node<E>> onAbsent) {
        synchronized (lock) {
            Node<E> pred = head, curr = pred.next;
            while (curr.item.compareTo(item) < 0) {
                pred = curr;
                curr = curr.next;
            }
            return curr.item.equals(item) ? onFound.test(pred, curr) : onAbsent.test(pred, curr);
        }
    }

    @Override
    public boolean add(E item) {
        BiPredicate<Node<E>, Node<E>> addNode = (pred, curr) -> {
            Node<E> node = new Node<>(item);
            node.next = curr;
            pred.next = node;
            return true;
        };
        return executeSynchronized(item, (pred, curr) -> false, addNode);
    }

    @Override
    public boolean remove(E item) {
        BiPredicate<Node<E>, Node<E>> removeNode = (pred, curr) -> {
            pred.next = curr.next;
            return true;
        };
        return executeSynchronized(item, removeNode, (pred, curr) -> false);
    }

    @Override
    public boolean contains(E item) {
        return executeSynchronized(item, (pred, curr) -> true, (pred, curr) -> false);
    }

    @Override
    public int getSize() {
        int counter = 0;
        for(Node<E> node = head.next; node.next != null; node = node.next) {
            counter++;
        }
        return counter;
    }
}
