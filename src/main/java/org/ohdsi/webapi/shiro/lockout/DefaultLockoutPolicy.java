package org.ohdsi.webapi.shiro.lockout;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultLockoutPolicy implements LockoutPolicy {

    private LockoutStrategy lockoutStrategy;
    private int maxAttempts = 3;
    private Map<String, LockEntry> lockEntryMap = new ConcurrentHashMap<>();
    private static final LockEntry DEFAULT_LOCK = new LockEntry(0, LockEntry.EXPIRED_NOT_SET);

    public DefaultLockoutPolicy(LockoutStrategy lockoutStrategy, int maxAttempts) {

        if (maxAttempts > 0) {
            this.lockoutStrategy = lockoutStrategy;
            this.maxAttempts = maxAttempts;
        } else {
            throw new IllegalArgumentException("maxAttempts should be greater than 0");
        }
    }

    @Override
    public boolean isLockedOut(String principal) {

        LockEntry lockEntry = lockEntryMap.getOrDefault(principal, DEFAULT_LOCK);
        long now = new Date().getTime();
        return lockEntry.getExpired() != LockEntry.EXPIRED_NOT_SET && now < lockEntry.getExpired();
    }

    @Override
    public long getLockExpiration(String principal) {

        LockEntry lockEntry = lockEntryMap.getOrDefault(principal, DEFAULT_LOCK);
        return lockEntry.getExpired();
    }

    @Override
    public void loginFailed(String principal) {

        LockEntry lockEntry = lockEntryMap.getOrDefault(principal,
                new LockEntry(0, LockEntry.EXPIRED_NOT_SET));
        int attempts = lockEntry.getAttempts();
        long expired = lockEntry.getExpired();
        if (++attempts >= maxAttempts){
            long duration = lockoutStrategy.getLockDuration(attempts);
            long now = new Date().getTime();
            expired = now + duration;
        }
        lockEntry = new LockEntry(attempts, expired);
        lockEntryMap.put(principal, lockEntry);
    }

    @Override
    public void loginSuceeded(String principal) {

        lockEntryMap.remove(principal);
    }

}
