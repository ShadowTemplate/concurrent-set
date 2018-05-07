package tester;

import sets.Set;

public class RemoverWorker implements Runnable {

    private final Set<Integer> set;

    public RemoverWorker(Set<Integer> set) {
        this.set = set;
    }

    @Override
    public void run() {
        Integer item = Tester.added.pollFirst();
        if (item != null) {
            set.remove(item);
        }
    }
}
