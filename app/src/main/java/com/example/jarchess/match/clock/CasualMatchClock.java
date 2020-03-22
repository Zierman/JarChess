package com.example.jarchess.match.clock;

import android.util.Log;

import com.example.jarchess.match.ChessColor;
import com.example.jarchess.testmode.TestableCurrentTime;

import static android.support.constraint.Constraints.TAG;
import static java.lang.Math.abs;

public class CasualMatchClock implements MatchClock {

    public static final long IGNORE_TOLERANCE = -1L;
    private final long[] currentDisplayTimeMillis = new long[]{0L, 0L};
    private final Object lock;
    private final MatchClockObserver observer;
    private ChessColor runningColor;
    private long startTimeMillis;
    private boolean stopHasBeenCalled;

    public CasualMatchClock(MatchClockObserver observer) {
        this.lock = this;
        this.observer = observer;
    }

    @Override
    public void start() {
        start(ChessColor.WHITE);
    }

    @Override
    public void start(ChessColor colorStartingTurn) {
        runningColor = colorStartingTurn;
        startTimeMillis = TestableCurrentTime.currentTimeMillis();
        final MatchClock matchClock = this;

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.d(TAG, "MatchClock's anonymous runnable is running on thread: " + Thread.currentThread().getName());

                    synchronized (lock) {
                        while (!stopHasBeenCalled) {
                            {
                                observer.observeMatchClock(matchClock);
                                lock.wait(100); // updates approximately every tenth of a second.
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // get out
                } finally {
                    Log.d(TAG, "done running thread: " + Thread.currentThread().getName());
                }
            }
        }, "casualClockThread").start();
    }

    @Override
    public boolean isRunning() {
        return !stopHasBeenCalled;
    }

    @Override
    public ChessColor getRunningColor() {
        return runningColor;
    }

    @Override
    public void syncEnd(ChessColor colorEndingTurn, long reportedElapsedTimeMillis, long toleranceMillis) throws ClockSyncException {

        if (!stopHasBeenCalled) {
            long measuredElapsedTime = TestableCurrentTime.currentTimeMillis() - startTimeMillis;
            if (colorEndingTurn == runningColor) {
                if (toleranceMillis >= 0 && abs(reportedElapsedTimeMillis - measuredElapsedTime) > toleranceMillis) {
                    throw new ClockSyncException(reportedElapsedTimeMillis, measuredElapsedTime, toleranceMillis);
                }

                // update the current time
                currentDisplayTimeMillis[colorEndingTurn.getIntValue()] += reportedElapsedTimeMillis;

                // toggle to next color
                runningColor = ChessColor.getOther(runningColor);
                startTimeMillis = TestableCurrentTime.currentTimeMillis();
            } else {
                Log.e(TAG, "syncEnd: Attempting to sync end on with the non running color");
            }
        } else {
            Log.e(TAG, "syncEnd: called after clock has stopped");
        }

    }

    @Override
    public void syncEnd(ChessColor colorEndingTurn, long reportedElapsedTimeMillis) {
        try {
            syncEnd(colorEndingTurn, reportedElapsedTimeMillis, IGNORE_TOLERANCE);
        } catch (ClockSyncException e) {
            Log.wtf(TAG, "syncEnd: this exception should not have been thrown", e);
        }
    }

    @Override
    public long getDisplayedTimeMillis(ChessColor colorTimeToGet) {
        if (runningColor != null) {

            long elapsed = TestableCurrentTime.currentTimeMillis() - startTimeMillis;
            long updatedCurrent = currentDisplayTimeMillis[runningColor.getIntValue()] + elapsed;
            return colorTimeToGet == runningColor ? updatedCurrent : currentDisplayTimeMillis[colorTimeToGet.getIntValue()];
        } else {
            return currentDisplayTimeMillis[colorTimeToGet.getIntValue()];
        }
    }

    @Override
    public boolean flagHasFallen() {
        // flag never falls
        return false;
    }

    @Override
    public void stop() {
        if (!stopHasBeenCalled) {
            stopHasBeenCalled = true;
            observer.observeMatchClock(this);
            runningColor = null;
        }
    }

    @Override
    public Object getLock() {
        return lock;
    }
}