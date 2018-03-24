package org.ohdsi.webapi.shiro.lockout;

import java.util.concurrent.TimeUnit;

public class ExponentLockoutStrategy implements LockoutStrategy {

    private final long initialDuration;
    private final long increment;
    private final int maxAttempts;

    public ExponentLockoutStrategy(long initialDuration, long increment, int maxAttempts) {

        this.initialDuration = TimeUnit.SECONDS.toMillis(initialDuration);
        this.increment = TimeUnit.SECONDS.toMillis(increment);
        this.maxAttempts = maxAttempts;
    }

    @Override
    public long getLockDuration(int attempts) {

        return initialDuration + (attempts - maxAttempts) * increment;
    }
}
