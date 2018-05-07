package sets;

import nodes.LockableNode;

import java.util.function.BiPredicate;

public class FineGrainedListSet<E extends Comparable<? super E>> implements Set<E> {

    private final LockableNode<E> head;

    public FineGrainedListSet(E minValue, E maxValue) {
        head = new LockableNode<>(minValue);
        head.next = new LockableNode<>(maxValue);
    }

    private boolean executeWithLockCoupling(E item, BiPredicate<LockableNode<E>, LockableNode<E>> onFound,
                                            BiPredicate<LockableNode<E>, LockableNode<E>> onAbsent) {
        head.lock();
        LockableNode<E> pred = head;
        try {
            LockableNode<E> curr = pred.next;
            curr.lock();
            try {
                while(curr.item.compareTo(item) < 0) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return curr.item.equals(item) ? onFound.test(pred, curr) : onAbsent.test(pred, curr);
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
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
        return executeWithLockCoupling(item, (pred, curr) -> false, addNode);
    }

    @Override
    public boolean remove(E item) {
        BiPredicate<LockableNode<E>, LockableNode<E>> removeNode = (pred, curr) -> {
            pred.next = curr.next;
            return true;
        };
        return executeWithLockCoupling(item, removeNode, (pred, curr) -> false);
    }

    @Override
    public boolean contains(E item) {
        return executeWithLockCoupling(item, (pred, curr) -> true, (pred, curr) -> false);
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
