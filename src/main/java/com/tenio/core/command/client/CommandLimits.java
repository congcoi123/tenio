/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.command.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Per-manager token buckets. Call configure during command registration.
 */
public final class CommandLimits {

    public record Counts(long accepted, long rejected) {
    }

    private final Map<Short, Rule> rules = new ConcurrentHashMap<>();
    private final LongSupplier clock;
    private volatile boolean enabled;

    boolean enabled() {
        return enabled;
    }

    public CommandLimits() {
        this(System::nanoTime);
    }

    CommandLimits(LongSupplier clock) {
        this.clock = clock;
    }

    public void configure(short code, int perSecond, int burst) {
        if (perSecond <= 0 || burst <= 0) {
            throw new IllegalArgumentException("Rate and burst must both be positive");
        }
        if (rules.putIfAbsent(code, new Rule(perSecond, burst)) != null) {
            throw new IllegalArgumentException("Duplicate command limit for " + code);
        }
        enabled = true;
    }

    public boolean allow(short code, String identity) {
        var rule = rules.get(code);
        return rule == null || rule.allow(identity, clock);
    }

    /**
     * Optional pre-login allowance shared only by requests on this session.
     */
    public boolean allowSession(short code, long sessionId) {
        var rule = rules.get(code);
        return rule == null || rule.allow(sessionId, clock);
    }

    public void releaseSession(long sessionId) {
        rules.values().forEach(rule -> rule.release(sessionId));
    }

    public Map<Short, Counts> counts() {
        var result = new HashMap<Short, Counts>();
        rules.forEach((code, rule) -> result.put(code, rule.counts()));
        return Map.copyOf(result);
    }

    public void clear() {
        rules.clear();
        enabled = false;
    }

    private record Bucket(long time, double tokens) {
    }

    private static final class Rule {

        private static final int MAX_IDENTITIES = 65536;

        private final LinkedHashMap<Object, Bucket> buckets = new LinkedHashMap<>(16, 0.75f, true);
        private final int rate;
        private final int burst;
        private final long refillNanos;
        private long accepted;
        private long rejected;

        Rule(int rate, int burst) {
            this.rate = rate;
            this.burst = burst;
            refillNanos = (long) Math.ceil(burst * 1_000_000_000.0 / rate);
        }

        synchronized boolean allow(Object identity, LongSupplier clock) {
            long now = clock.getAsLong();
            // Once full, a bucket is equivalent to a new identity.
            // Bound cleanup work per request after a quiet period.
            for (int i = 0; i < 8 && !buckets.isEmpty(); i++) {
                if (now - buckets.firstEntry().getValue().time() < refillNanos) {
                    break;
                }
                buckets.pollFirstEntry();
            }
            var previous = buckets.get(identity);
            if (identity == null || (previous == null && buckets.size() >= MAX_IDENTITIES)) {
                rejected++;
                return false;
            }
            double tokens = previous == null ? burst : Math.min(burst,
                    previous.tokens() + (now - previous.time()) * (double) rate / 1_000_000_000);
            if (tokens < 1) {
                buckets.put(identity, new Bucket(now, tokens));
                rejected++;
                return false;
            }
            buckets.put(identity, new Bucket(now, tokens - 1));
            accepted++;
            return true;
        }

        synchronized void release(long sessionId) {
            buckets.remove(sessionId);
        }

        synchronized Counts counts() {
            return new Counts(accepted, rejected);
        }
    }
}
