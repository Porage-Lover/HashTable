import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsDashboard {
    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();
    private int totalViews = 0;

    public synchronized void processEvent(String url, String userId, String source) {
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);
        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
        totalViews++;
    }

    public synchronized String getDashboard() {
        StringBuilder sb = new StringBuilder("getDashboard() ->\nTop Pages:\n");
        
        int rank = 1;
        List<Map.Entry<String, Integer>> sortedPages = new ArrayList<>(pageViews.entrySet());
        sortedPages.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        for(Map.Entry<String, Integer> e : sortedPages) {
            sb.append(String.format("%d. %s - %d views (%d unique)\n", 
                rank++, e.getKey(), e.getValue(), uniqueVisitors.get(e.getKey()).size()));
            if(rank > 10) break;
        }

        sb.append("\nTraffic Sources:\n");
        trafficSources.forEach((source, count) -> {
            double pct = (double) count / totalViews * 100;
            sb.append(String.format("%s: %.0f%%, ", source.substring(0, 1).toUpperCase() + source.substring(1), pct));
        });
        
        // Remove trailing comma and space
        if (sb.length() > 2) sb.setLength(sb.length() - 2); 
        return sb.toString();
    }

    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        
        System.out.println("processEvent({url: \"/article/breaking-news\", userId: \"user_123\", source: \"google\"})");
        dashboard.processEvent("/article/breaking-news", "user_123", "google");
        
        System.out.println("processEvent({url: \"/article/breaking-news\", userId: \"user_456\", source: \"facebook\"})");
        dashboard.processEvent("/article/breaking-news", "user_456", "facebook");
        
        // Simulate more traffic to match the prompt closely
        for(int i=0; i<4; i++) dashboard.processEvent("/article/breaking-news", "user_" + i, "google");
        for(int i=0; i<4; i++) dashboard.processEvent("/sports/championship", "user_x" + i, "direct");
        
        System.out.println("...");
        System.out.println(dashboard.getDashboard());
    }
}