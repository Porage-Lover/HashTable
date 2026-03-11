import java.util.LinkedHashMap;
import java.util.Map;

public class DNSCache {
    private static class DNSEntry {
        String ipAddress;
        long expiryTime;

        DNSEntry(String ipAddress, long ttlSeconds) {
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }
        boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }

    private final int capacity;
    private int hits = 0, misses = 0;
    
    private final Map<String, DNSEntry> cache = new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
            return size() > capacity;
        }
    };

    public DNSCache(int capacity) {
        this.capacity = capacity;
    }

    public String resolve(String domain, long ttlOverride) {
        DNSEntry entry = cache.get(domain);
        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT -> " + entry.ipAddress;
        }
        
        misses++;
        if (entry != null) cache.remove(domain);
        
        // Mock upstream query
        String resolvedIp = domain.equals("google.com") ? "172.217.14.206" : "10.0.0.1";
        cache.put(domain, new DNSEntry(resolvedIp, ttlOverride));
        
        return (entry != null && entry.isExpired() ? "Cache EXPIRED" : "Cache MISS") + 
               " -> Query upstream -> " + resolvedIp + " (TTL: " + ttlOverride + "s)";
    }

    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (double) hits / total * 100;
        return String.format("Hit Rate: %.1f%%, Avg Lookup Time: 0.8ms", hitRate); // 0.8ms mocked for demo
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache(100);
        
        // Using a 1-second TTL for the demo instead of waiting 301 seconds
        System.out.println("resolve(\"google.com\") -> " + dns.resolve("google.com", 1));
        System.out.println("resolve(\"google.com\") -> " + dns.resolve("google.com", 1));
        
        System.out.println("... waiting for TTL to expire ...");
        Thread.sleep(1100); // Wait 1.1 seconds
        
        System.out.println("resolve(\"google.com\") -> " + dns.resolve("google.com", 300));
        System.out.println("getCacheStats() -> " + dns.getCacheStats());
    }
}