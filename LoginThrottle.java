package com.jobhub.util;

import com.jobhub.exception.JobHubException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe brute-force protection: after MAX_FAILS wrong passwords for an e-mail,
 * further logins for that e-mail are refused for LOCK_MS. Servlets are multi-threaded,
 * so the state lives in a ConcurrentHashMap and is only changed inside compute().
 */
public class LoginThrottle {
    private static final int MAX_FAILS = 5;
    private static final long LOCK_MS = 5 * 60_000L;

    private static final class Attempt {
        int fails;
        volatile long lockedUntil;
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Attempt a = attempts.get(key);
        if (a != null && a.lockedUntil > System.currentTimeMillis())
            throw new JobHubException(429, "Too many failed attempts. Try again in a few minutes.");
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, a) -> {
            if (a == null || (a.lockedUntil != 0 && a.lockedUntil <= System.currentTimeMillis())) a = new Attempt();
            if (++a.fails >= MAX_FAILS) { a.lockedUntil = System.currentTimeMillis() + LOCK_MS; a.fails = 0; }
            return a;
        });
    }

    public void reset(String key) { attempts.remove(key); }
}
