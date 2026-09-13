package br.com.fiap.esg.mobilidade_sustentavel.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    @Test
    void exhaustedClientRemainsLimitedWhenStorageIsFull() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(() -> 0L, 1);
        for (int i = 0; i < 10; i++) {
            assertEquals(200, request(filter, "client-a", "/api/auth/login"));
        }
        assertEquals(429, request(filter, "client-a", "/api/auth/login"));
        assertEquals(429, request(filter, "client-b", "/api/auth/login"));
        assertEquals(429, request(filter, "client-a", "/api/auth/login"));
        assertEquals(200, request(filter, "client-b", "/actuator/health"));
    }

    @Test
    void idleClientsExpireWhileRecentlyUsedClientsKeepTheirQuota() throws Exception {
        AtomicLong time = new AtomicLong();
        RateLimitFilter filter = new RateLimitFilter(time::get, 2);
        assertEquals(200, request(filter, "idle", "/api/auth/login"));
        time.set(Duration.ofMinutes(9).toNanos());
        for (int i = 0; i < 10; i++) {
            assertEquals(200, request(filter, "active", "/api/auth/login"));
        }
        time.set(Duration.ofMinutes(10).toNanos());
        assertEquals(200, request(filter, "new", "/api/auth/login"));
        assertEquals(429, request(filter, "active", "/api/auth/login"));
        assertEquals(429, request(filter, "another", "/api/auth/login"));
    }

    @Test
    void simultaneousRequestsCannotExceedTheClientQuota() {
        RateLimitFilter filter = new RateLimitFilter(() -> 0L, 1);
        long accepted = IntStream.range(0, 100).parallel().filter(i -> {
            try {
                return request(filter, "same-client", "/api/auth/login") == 200;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).count();
        assertEquals(10, accepted);
    }

    private int request(RateLimitFilter filter, String ip, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> { });
        return response.getStatus();
    }
}
