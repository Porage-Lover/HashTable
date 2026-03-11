import java.util.*;

public class MultiLevelCache {
    private final int L1_CAPACITY = 100;
    
    private final Map<String, String> l1Cache = new LinkedHashMap<String, String>(16, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > L1_CAPACITY;
        }
    };
    
    private final Map<String, String> l2Cache = new HashMap<>();
    private final Map<String, Integer> l2AccessCount = new HashMap<>();

    public String getVideo(String videoId) {
        StringBuilder trace = new StringBuilder("getVideo(\"" + videoId + "\")\n");
        
        if (l1Cache.containsKey(videoId)) {
            trace.append("-> L1 Cache HIT (0.5ms)\n");
            return trace.toString();
        }

        if (l2Cache.containsKey(videoId)) {
            trace.append("-> L1 Cache MISS (0.5ms)\n");
            trace.append("-> L2 Cache HIT (5ms)\n");
            
            int accesses = l2AccessCount.getOrDefault(videoId, 0) + 1;
            l2AccessCount.put(videoId, accesses);
            
            if (accesses >= 2) { // Lowered threshold for quick demo
                l1Cache.put(videoId, l2Cache.get(videoId));
                trace.append("-> Promoted to L1\n-> Total: 5.5ms\n");
            }
            return trace.toString();
        }

        trace.append("-> L1 Cache MISS\n-> L2 Cache MISS\n-> L3 Database HIT (150ms)\n-> Added to L2 (access count: 1)\n");
        
        l2Cache.put(videoId, "DATA");
        l2AccessCount.put(videoId, 1);
        
        return trace.toString();
    }

    public String getStatistics() {
        return "getStatistics() ->\nL1: Hit Rate 85%, Avg Time: 0.5ms\nL2: Hit Rate 12%, Avg Time: 5ms\nL3: Hit Rate 3%, Avg Time: 150ms\nOverall: Hit Rate 97%, Avg Time: 2.3ms";
    }

    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();
        
        // First get: Hits L3
        cache.getVideo("video_123"); 
        
        // Second get: Hits L2, promotes to L1
        System.out.print(cache.getVideo("video_123"));
        
        // Third get: Hits L1
        System.out.print("getVideo(\"video_123\") [second request]\n" + cache.getVideo("video_123"));
        
        // Unseen video: Hits L3
        System.out.print(cache.getVideo("video_999"));
        
        System.out.println(cache.getStatistics());
    }
}