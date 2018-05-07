package nodes;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockableNode<E extends Comparable<? super E>> extends Node<E> {

    private final Lock lock = new ReentrantLock();
    public LockableNode<E> next = null;

    public LockableNode(E item) {
        super(item);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}


