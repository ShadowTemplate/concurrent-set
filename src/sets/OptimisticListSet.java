package sets;

import nodes.LockableNode;

import java.util.function.BiPredicate;

public class OptimisticListSet<E extends Comparable<? super E>> implements Set<E> {

    private final LockableNode<E> head;

    public OptimisticListSet(E minValue, E maxValue) {
        head = new LockableNode<>(minValue);
        head.next = new LockableNode<>(maxValue);
    }

    private boolean validate(LockableNode<E> pred, LockableNode<E> curr) {
        LockableNode<E> node = head;
        while(node.item.compareTo(pred.item) <= 0) {
            if (node == pred) {
                return pred.next == curr;
            }
            node = node.next;
        }
        return false;
    }

    private boolean executeOptimistically(E item, BiPredicate<LockableNode<E>, LockableNode<E>> onFound,
                                            BiPredicate<LockableNode<E>, LockableNode<E>> onAbsent) {
        while (true) {
            LockableNode<E> pred = head;
            LockableNode<E> curr = pred.next;
            while (curr.item.compareTo(item) < 0) {
                pred = curr;
                curr = curr.next;
            }
            synchronized (pred) {
                synchronized (curr) {
                    if (validate(pred, curr)) {
                        return curr.item.equals(item) ? onFound.test(pred, curr) : onAbsent.test(pred, curr);
                    }
                }
            }
        }
    }

    @Override
    public boolean add(E item) {
        BiPredicate<LockableNode<E>, LockableNode<E>> addNode = (pred, curr) -> {
            LockableNode<E> node = new LockableNode<>(item);
            node.next = curr;
            pred.next = node;
            return true;
        };
        return executeOptimistically(item, (pred, curr) -> false, addNode);
    }

    @Override
    public boolean remove(E item) {
        BiPredicate<LockableNode<E>, LockableNode<E>> removeNode = (pred, curr) -> {
            pred.next = curr.next;
            return true;
        };
        return executeOptimistically(item, removeNode, (pred, curr) -> false);
    }

    @Override
    public boolean contains(E item) {
        return executeOptimistically(item, (pred, curr) -> true, (pred, curr) -> false);
    }

    @Override
    public int getSize() {
        int counter = 0;
        for(LockableNode<E> node = head.next; node.next != null; node = node.next) {
            counter++;
        }
        return counter;
    }
}

