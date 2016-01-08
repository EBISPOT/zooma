package uk.ac.ebi.fgpt.zooma.search;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Abstract class for generating ZOOMA reports.  Specific implementations, generating specific types of reports,
 *
 * @author Tony Burdett
 * @date 28-Jun-2010
 */
public class ZOOMASearchTimer {
    private final int totalCount;
    private final int sampleSize;
    private final int queueSize;

    private int completedCount;

    private Deque<Long> benchmarkTimes;
    private Deque<Long> estimates;

    private long startTime;
    private long lastEstimate;

    public ZOOMASearchTimer(int totalCount) {
        this(totalCount, totalCount / 10); // use default sample size of 10%
    }

    public ZOOMASearchTimer(int totalCount, int sampleSize) {
        this.totalCount = totalCount;
        this.sampleSize = sampleSize == 0
                ? 1
                : sampleSize;
        this.queueSize = (sampleSize / 10) == 0
                ? 1
                : (sampleSize / 10); // calculate rolling average of last 10% of estimates

        this.completedCount = 0;

        benchmarkTimes = new LinkedBlockingDeque<>(this.sampleSize);
        estimates = new LinkedBlockingDeque<>(this.queueSize);
    }

    public synchronized ZOOMASearchTimer start() {
        startTime = System.currentTimeMillis();
        return this;
    }

    public synchronized ZOOMASearchTimer finish() {
        while (completedCount < totalCount) {
            completedNext();
        }
        return this;
    }

    public synchronized ZOOMASearchTimer completedNext() {
        long finishTime, benchmarkTime, average;
        int remaining, sampled;

        finishTime = System.currentTimeMillis();

        completedCount++;
        benchmarkTimes.add(finishTime);

        // have we reached our sample size?
        if (benchmarkTimes.size() == sampleSize) {
            benchmarkTime = benchmarkTimes.poll();
            sampled = sampleSize;
        }
        else {
            benchmarkTime = startTime;
            sampled = completedCount;
        }

        // calculate an estimate
        average = (finishTime - benchmarkTime) / sampled;
        remaining = totalCount - completedCount;
        long nextEstimate = average * remaining;
        if (estimates.size() == queueSize) {
            estimates.pop();
        }
        estimates.add(nextEstimate);

        // and calculate rolling average
        long total = 0;
        for (long estimate : estimates) {
            total += estimate;
        }
        lastEstimate = total / (estimates.size());

        return this;
    }

    public synchronized int getCompletedCount() {
        return completedCount;
    }

    public synchronized int getTotalCount() {
        return totalCount;
    }

    public synchronized long getCurrentEstimate() {
        return lastEstimate;
    }
}
