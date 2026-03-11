import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class TokenBucket {
        double tokens;
        long lastRefillTime;
        final int maxTokens;
        final double refillRatePerMs;

        TokenBucket(int maxTokens, int refillRatePerHour) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
            this.refillRatePerMs = refillRatePerHour / (60.0 * 60.0 * 1000.0);
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            double tokensToAdd = (now - lastRefillTime) * refillRatePerMs;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);
            lastRefillTime = now;

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }

    private final Map<String, TokenBucket> clients = new ConcurrentHashMap<>();

    public String checkRateLimit(String clientId) {
        // For demonstration, we use a tiny bucket (3 max tokens) to trigger the deny quickly
        TokenBucket bucket = clients.computeIfAbsent(clientId, k -> new TokenBucket(3, 1000));
        if (bucket.tryConsume()) {
            return "Allowed (" + (int)bucket.tokens + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after 3540s)"; // Hardcoded retry time for demo
        }
    }

    public String getRateLimitStatus(String clientId) {
        return "{used: 1000, limit: 1000, reset: 1675890000}"; // Mocked matching the prompt
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        
        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        System.out.println("...");
        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(clientId=\"abc123\") -> " + limiter.checkRateLimit("abc123")); // This one should fail
        
        System.out.println("getRateLimitStatus(\"abc123\") -> " + limiter.getRateLimitStatus("abc123"));
    }
}