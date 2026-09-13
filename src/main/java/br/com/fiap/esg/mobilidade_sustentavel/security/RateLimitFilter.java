package br.com.fiap.esg.mobilidade_sustentavel.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.function.LongSupplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public final class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private static final long IDLE_TIMEOUT_NANOS = Duration.ofMinutes(10).toNanos();

    private final Map<String, BucketEntry> buckets = new LinkedHashMap<>(16, 0.75f, true);
    private final LongSupplier ticker;
    private final int maxTrackedClients;

    public RateLimitFilter() {
        this(System::nanoTime, MAX_TRACKED_CLIENTS);
    }

    RateLimitFilter(LongSupplier ticker, int maxTrackedClients) {
        if (maxTrackedClients < 1) {
            throw new IllegalArgumentException("Client capacity must be positive");
        }
        this.ticker = ticker;
        this.maxTrackedClients = maxTrackedClients;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/auth/")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = bucketFor(ip);
            if (bucket == null || !bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again in a minute.\"}"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private synchronized Bucket bucketFor(String ip) {
        long now = ticker.getAsLong();
        Iterator<BucketEntry> entries = buckets.values().iterator();
        while (entries.hasNext()) {
            if (now - entries.next().lastSeen < IDLE_TIMEOUT_NANOS) {
                break;
            }
            entries.remove();
        }

        BucketEntry entry = buckets.get(ip);
        if (entry != null) {
            entry.lastSeen = now;
            return entry.bucket;
        }
        // Reject new clients at capacity rather than resetting an active client's quota.
        if (buckets.size() >= maxTrackedClients) {
            return null;
        }
        Bucket bucket = newBucket();
        buckets.put(ip, new BucketEntry(bucket, now));
        return bucket;
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private long lastSeen;

        private BucketEntry(Bucket bucket, long lastSeen) {
            this.bucket = bucket;
            this.lastSeen = lastSeen;
        }
    }

    private Bucket newBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build())
            .build();
    }
}
