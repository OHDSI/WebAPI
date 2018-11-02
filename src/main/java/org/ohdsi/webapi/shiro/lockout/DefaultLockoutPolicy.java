package org.ohdsi.webapi.shiro.lockout;

import com.odysseusinc.logging.event.LockoutStartEvent;
import com.odysseusinc.logging.event.LockoutStopEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultLockoutPolicy implements LockoutPolicy {

    private LockoutStrategy lockoutStrategy;
    private int maxAttempts;
    private Map<String, LockEntry> lockEntryMap = new ConcurrentHashMap<>();
    private static final LockEntry DEFAULT_LOCK = new LockEntry(0, LockEntry.EXPIRED_NOT_SET);
    private ApplicationEventPublisher eventPublisher;

    public DefaultLockoutPolicy(LockoutStrategy lockoutStrategy, int maxAttempts, ApplicationEventPublisher eventPublisher) {

        if (maxAttempts > 0) {
            this.lockoutStrategy = lockoutStrategy;
            this.maxAttempts = maxAttempts;
        } else {
            throw new IllegalArgumentException("maxAttempts should be greater than 0");
        }
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 60000)
    private void checkLockout() {
        lockEntryMap.forEach((principal, entry) -> {
            if (!isLockedOut(entry) && entry.getAttempts() >= maxAttempts) {
                eventPublisher.publishEvent(new LockoutStopEvent(this, principal));
                lockEntryMap.remove(principal);
            }
        });
    }

    @Override
    public boolean isLockedOut(String principal) {

        LockEntry lockEntry = lockEntryMap.getOrDefault(principal, DEFAULT_LOCK);
        long now = new Date().getTime();
        return lockEntry.getExpired() != LockEntry.EXPIRED_NOT_SET && now < lockEntry.getExpired();
    }

    @Override
    public boolean isLockedOut(LockEntry entry) {

        long now = new Date().getTime();
        return entry.getExpired() != LockEntry.EXPIRED_NOT_SET && now < entry.getExpired();
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
        if (++attempts >= maxAttempts) {
            if (!isLockedOut(principal)) {
                eventPublisher.publishEvent(new LockoutStartEvent(this, principal));
            }
            long duration = lockoutStrategy.getLockDuration(attempts);
            long now = new Date().getTime();
            expired = now + duration;
        }
        lockEntry = new LockEntry(attempts, expired);
        lockEntryMap.put(principal, lockEntry);
    }

    @Override
    public void loginSucceeded(String principal) {

        lockEntryMap.remove(principal);
    }

}
