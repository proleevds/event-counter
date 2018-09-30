package com.prol;

import com.prol.impl.EventCounter;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class TestEventCounter {
    private IEventCounter counter;

    @Before
    public void createTestCounter() {
        counter = new EventCounter(Duration.standardMinutes(1));
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals(0L, counter.computeStatistics(Duration.ZERO));
        Assert.assertEquals(0L, counter.computeStatistics(Duration.standardSeconds(1)));
        Assert.assertEquals(0L, counter.computeStatistics(Duration.standardMinutes(1)));
    }

    @Test
    public void simpleTest() {
        counter.register(DateTime.now());
        counter.register(DateTime.now());
        counter.register(DateTime.now());
        Assert.assertEquals(3L, counter.computeStatistics(Duration.standardMinutes(1)));
        Assert.assertEquals(3L, counter.computeStatistics(Duration.standardSeconds(1)));
        Assert.assertEquals(0L, counter.computeStatistics(Duration.ZERO));
    }

    @Test
    public void testDifferenceInMinutes() {
        final DateTime now = DateTime.now();
        counter.register(now.minusMinutes(1));
        counter.register(now.minusMinutes(2));
        counter.register(now.minusMinutes(3));

        Assert.assertEquals(0L, counter.computeStatistics(Duration.standardMinutes(1)));
        Assert.assertEquals(1L, counter.computeStatistics(Duration.standardMinutes(2)));
        Assert.assertEquals(2L, counter.computeStatistics(Duration.standardMinutes(3)));
        Assert.assertEquals(3L, counter.computeStatistics(Duration.standardMinutes(4)));
    }

    @Test
    public void testBulkLoad() throws ExecutionException, InterruptedException {
        final int bulkLoadIndex = 100_000;
        final int nThreads = 25;

        final Executor executor = new ForkJoinPool(nThreads);
        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < bulkLoadIndex; j++) {
                    counter.register(DateTime.now().minusSeconds(Math.abs(new Random().nextInt(10_000_000))));
                }
            }, executor));
        }
        for (CompletableFuture future : futures) {
            future.get();
        }

        Assert.assertEquals(bulkLoadIndex * nThreads,
                counter.computeStatistics(Duration.standardSeconds(Integer.MAX_VALUE)));
    }
}
