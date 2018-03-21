package org.ohdsi.webapi.shiro.lockout;

import java.util.Objects;

class LockEntry {
    private int attempts;
    private long expired = 0L;
    public static final long EXPIRED_NOT_SET = -1L;

    public LockEntry(int attempts, long expired) {

        this.attempts = attempts;
        this.expired = expired;
    }

    public int getAttempts() {

        return attempts;
    }

    public long getExpired() {

        return expired;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof LockEntry)) return false;
        LockEntry lockEntry = (LockEntry) o;
        return attempts == lockEntry.attempts &&
                expired == lockEntry.expired;
    }

    @Override
    public int hashCode() {

        return Objects.hash(attempts, expired);
    }
}
