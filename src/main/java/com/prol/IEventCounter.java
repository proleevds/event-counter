package com.prol;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public interface IEventCounter {
    void register(DateTime event);

    long computeStatistics(Duration duration);
}
