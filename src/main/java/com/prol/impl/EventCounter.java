package com.prol.impl;

import com.prol.IEventCounter;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EventCounter implements IEventCounter {
    private static final Duration DEFAULT_MAX_STATISTIC_PERIOD = Duration.standardHours(24);
    private static final Duration CLEAR_OUTDATED_STATISTICS_PERIOD = Duration.standardMinutes(1);

    private final Map<DateTime, AtomicLong> statistics;
    private final Duration maxPeriod;
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(2);

    public EventCounter() {
        this(DEFAULT_MAX_STATISTIC_PERIOD);
    }

    public EventCounter(final Duration maxPeriod) {
        this.statistics = new ConcurrentHashMap<>();
        this.maxPeriod = maxPeriod;
        cleanupExecutor.scheduleWithFixedDelay(() -> {
                    final List<DateTime> outdatedStatisticKeys = statistics.entrySet().stream()
                            .filter(e -> e.getKey().isBefore(round(DateTime.now()).minus(maxPeriod)))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    outdatedStatisticKeys.forEach(statistics::remove);
                },
                CLEAR_OUTDATED_STATISTICS_PERIOD.getStandardSeconds(),
                CLEAR_OUTDATED_STATISTICS_PERIOD.getStandardSeconds(),
                TimeUnit.SECONDS);
    }

    @Override
    public void register(DateTime timestamp) {
        statistics.computeIfAbsent(round(timestamp),
                key -> new AtomicLong())
                .getAndIncrement();
    }

    @Override
    public long computeStatistics(final Duration duration) {
        return statistics.entrySet().stream()
                .filter(e -> e.getKey().isAfter(round(DateTime.now()).minus(duration)))
                .mapToLong(e -> e.getValue().get())
                .sum();
    }

    private DateTime round(final DateTime value) {
        return value.withMillisOfSecond(0).withSecondOfMinute(0);
    }
}