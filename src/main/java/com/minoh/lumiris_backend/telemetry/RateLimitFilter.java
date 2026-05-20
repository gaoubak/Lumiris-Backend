package com.minoh.lumiris_backend.telemetry;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String PATH_PREFIX = "/api/telemetry/";
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_NANOS = 60_000_000_000L;
    private static final int MAX_TRACKED_IPS = 10_000;

    private final Map<String, AtomicReference<Bucket>> buckets = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_TRACKED_IPS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AtomicReference<Bucket>> eldest) {
                    return size() > MAX_TRACKED_IPS;
                }
            }
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = clientIp(request);
        if (!allow(ip)) {
            log.warn("rate_limit_exceeded ip={} path={}", ip, request.getRequestURI());
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean allow(String ip) {
        AtomicReference<Bucket> ref = buckets.computeIfAbsent(ip, k -> new AtomicReference<>(new Bucket(System.nanoTime(), 0)));
        while (true) {
            Bucket current = ref.get();
            long now = System.nanoTime();
            int count = (now - current.windowStart()) >= WINDOW_NANOS ? 1 : current.count() + 1;
            long start = (now - current.windowStart()) >= WINDOW_NANOS ? now : current.windowStart();
            if (count > MAX_REQUESTS_PER_MINUTE) return false;
            if (ref.compareAndSet(current, new Bucket(start, count))) return true;
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    private record Bucket(long windowStart, int count) {}
}
