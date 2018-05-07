package sets;

import nodes.AtomicMarkableNode;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.BiPredicate;

public class NonBlockingListSet<E extends Comparable<? super E>> implements Set<E> {

    private final AtomicMarkableNode<E> head;

    public NonBlockingListSet(E minValue, E maxValue) {
        head = new AtomicMarkableNode<>(minValue);
        head.next = new AtomicMarkableReference<>(new AtomicMarkableNode<>(maxValue), false);
    }

    private boolean executeWithCleaning(E item, BiPredicate<AtomicMarkableNode<E>, AtomicMarkableNode<E>> onFound,
                                        BiPredicate<AtomicMarkableNode<E>, AtomicMarkableNode<E>> onAbsent) {
        AtomicMarkableNode<E> pred, curr, succ;
        boolean marked[] = {false};
        pred = head;
        curr = pred.next.getReference();
        while (true) {
            succ = curr.next.get(marked);
            while (marked[0]) {  // marked[0] relates to curr node (and is fetched via succ)
                if (!pred.next.compareAndSet(curr, succ, false, false)) {
                    return executeWithCleaning(item, onFound, onAbsent);
                }
                curr = succ;
                succ = curr.next.get(marked);
            }
            if (curr.item.compareTo(item) >= 0) {
                return curr.item.equals(item) ? onFound.test(pred, curr) : onAbsent.test(pred, curr);
            }
            pred = curr;
            curr = succ;
        }
    }

    @Override
    public boolean add(E item) {
        BiPredicate<AtomicMarkableNode<E>, AtomicMarkableNode<E>> addNode = (pred, curr) -> {
            AtomicMarkableNode<E> node = new AtomicMarkableNode<>(item);
            node.next = new AtomicMarkableReference<>(curr, false);
            return pred.next.compareAndSet(curr, node, false, false) || add(item);
        };
        return executeWithCleaning(item, (pred, curr) -> false, addNode);
    }

    @Override
    public boolean remove(E item) {
        BiPredicate<AtomicMarkableNode<E>, AtomicMarkableNode<E>> removeNode = (pred, curr) -> {
            AtomicMarkableNode<E> succ = curr.next.getReference();
            if (!curr.next.compareAndSet(succ, succ, false, true)) {
                return remove(item);
            }
            pred.next.compareAndSet(curr, succ, false, false);
            return true;
        };
        return executeWithCleaning(item, removeNode, (pred, curr) -> false);
    }

    @Override
    public boolean contains(E item) {
        boolean marked[] = {false};
        AtomicMarkableNode<E> curr = head;
        while(curr.item.compareTo(item) < 0) {
            curr = curr.next.getReference();
            curr.next.get(marked);
        }
        return curr.item.equals(item) && !marked[0];
    }

    @Override
    public int getSize() {
        int counter = 0;
        for(AtomicMarkableNode<E> node = head.next.getReference(); node.next.getReference() != null; node = node.next.getReference()) {
            counter++;
        }
        return counter;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        AtomicMarkableNode<E> node = head, next;
        boolean marked[] = {false};
        while (true) {
            next = node.next.get(marked);
            sb.append("[").append(node).append("] I: ").append(node.item)
                    .append(" -> ([").append(next).append("], ").append(marked[0]).append(")\n");
            node = next;
            if (next == null) {
                break;
            }
        }
        System.out.println(sb.toString());
    }
}
