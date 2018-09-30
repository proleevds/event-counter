package com.prol;

import com.prol.impl.EventCounter;
import org.joda.time.DateTime;
import org.joda.time.Duration;

public class EventCounterFacade {
    private final IEventCounter eventCounter;

    public EventCounterFacade() {
        eventCounter = new EventCounter();
    }

    public void register(DateTime eventTimestamp) {
        eventCounter.register(eventTimestamp);
    }

    public long countEventsForLast1Minute() {
        return eventCounter.computeStatistics(Duration.standardMinutes(1));
    }

    public long countEventsForLast1Hour() {
        return eventCounter.computeStatistics(Duration.standardHours(1));
    }

    public long countEventsForLast24Hour() {
        return eventCounter.computeStatistics(Duration.standardHours(24));
    }
}
