package tester;

import sets.NonBlockingListSet;
import sets.Set;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tester {

    public static final Random random = new Random(1);
    public static final ConcurrentSkipListSet<Integer> added = new ConcurrentSkipListSet<>();
    public static final int itemsToAdd = 5000;

    public static void main(String[] args) {
        List<Set<Integer>> sets = Arrays.asList(
                new sets.CoarseGrainedListSet<>(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new sets.FineGrainedListSet<>(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new sets.LazyListSet<>(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new NonBlockingListSet<>(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new sets.OptimisticListSet<>(Integer.MIN_VALUE, Integer.MAX_VALUE));

        for(Set<Integer> s : sets) {
            System.out.println("Testing " + s.getClass());
            System.out.println("Initial set size: " + s.getSize());
            System.out.println("Adding items...");
            ExecutorService executor = Executors.newFixedThreadPool(1000);
            for (int i = 0; i < itemsToAdd; i++) {
                executor.execute(new AdderWorker(s));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("Items added: " + added.size());
            int setSize = s.getSize();
            System.out.println("Set size: " + setSize);
            System.out.println("Removing items...");

            executor = Executors.newFixedThreadPool(1000);
            for (int i = 0; i < setSize; i++) {
                executor.execute(new RemoverWorker(s));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("Set size: " + s.getSize() + "\n");
        }
    }
}
