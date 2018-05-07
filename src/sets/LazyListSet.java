package sets;

import nodes.MarkableLockableNode;

import java.util.function.BiPredicate;

public class LazyListSet<E extends Comparable<? super E>> implements Set<E> {

    private final MarkableLockableNode<E> head;

    public LazyListSet(E minValue, E maxValue) {
        head = new MarkableLockableNode<>(minValue);
        head.next = new MarkableLockableNode<>(maxValue);
    }

    private boolean validate(MarkableLockableNode<E> pred, MarkableLockableNode<E> curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }

    private boolean executeLazily(E item, BiPredicate<MarkableLockableNode<E>, MarkableLockableNode<E>> onFound,
                                          BiPredicate<MarkableLockableNode<E>, MarkableLockableNode<E>> onAbsent) {
        while (true) {
            MarkableLockableNode<E> pred = head;
            MarkableLockableNode<E> curr = pred.next;
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
        BiPredicate<MarkableLockableNode<E>, MarkableLockableNode<E>> addNode = (pred, curr) -> {
            MarkableLockableNode<E> node = new MarkableLockableNode<>(item);
            node.next = curr;
            pred.next = node;
            return true;
        };
        return executeLazily(item, (pred, curr) -> false, addNode);
    }

    @Override
    public boolean remove(E item) {
        BiPredicate<MarkableLockableNode<E>, MarkableLockableNode<E>> removeNode = (pred, curr) -> {
            curr.marked = true;
            pred.next = curr.next;
            return true;
        };
        return executeLazily(item, removeNode, (pred, curr) -> false);
    }
    
    @Override
    public boolean contains(E item) {
        MarkableLockableNode<E> curr = head;
        while (curr.item.compareTo(item) < 0) {
            curr = curr.next;
        }
        return curr.item.equals(item) && !curr.marked;
    }

    @Override
    public int getSize() {
        int counter = 0;
        for(MarkableLockableNode<E> node = head.next; node.next != null; node = node.next) {
            counter++;
        }
        return counter;
    }
}
