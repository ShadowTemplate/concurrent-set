package nodes;

public class MarkableLockableNode<E extends Comparable<? super E>> extends LockableNode<E> {

    public boolean marked = false;
    public MarkableLockableNode<E> next = null;

    public MarkableLockableNode(E item) {
        super(item);
    }
}
