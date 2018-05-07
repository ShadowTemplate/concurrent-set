package tester;

import sets.Set;

public class AdderWorker implements Runnable {

    private final Set<Integer> set;

    public AdderWorker(Set<Integer> set) {
        this.set = set;
    }

    @Override
    public void run() {
        int r = Tester.random.nextInt(Tester.itemsToAdd);
        Tester.added.add(r);
        set.add(r);
    }

}
