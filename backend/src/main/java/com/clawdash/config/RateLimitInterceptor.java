package com.clawdash.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, RateLimitInfo> limiters = new ConcurrentHashMap<>();
    private static final int DEFAULT_RATE = 100;
    private static final long WINDOW_MS = 1000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        RateLimitInfo info = limiters.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        
        info.cleanup();
        if (info.getCount() >= DEFAULT_RATE) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":429,\"message\":\"Rate limit exceeded\"}");
            return false;
        }
        info.increment();
        
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class RateLimitInfo {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        public int getCount() {
            return count.get();
        }

        public void increment() {
            count.incrementAndGet();
        }

        public void cleanup() {
            long now = System.currentTimeMillis();
            if (now - windowStart.get() > WINDOW_MS) {
                if (windowStart.compareAndSet(windowStart.get(), now)) {
                    count.set(0);
                }
            }
        }
    }
}
