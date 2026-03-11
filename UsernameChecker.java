import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameChecker {
    private final Map<String, String> registeredUsers = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> attemptFrequency = new ConcurrentHashMap<>();

    public boolean checkAvailability(String username) {
        attemptFrequency.computeIfAbsent(username, k -> new AtomicInteger(0)).incrementAndGet();
        return !registeredUsers.containsKey(username);
    }

    public void registerUser(String username, String userId) {
        if (checkAvailability(username)) {
            registeredUsers.put(username, userId);
        }
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int counter = 1;
        while (suggestions.size() < 3) {
            String alt1 = username + counter;
            String alt2 = username.replace("_", ".") + counter; // simple variation
            String alt3 = username + "_" + counter;
            
            if (!registeredUsers.containsKey(alt1) && suggestions.size() < 3) suggestions.add(alt1);
            if (!registeredUsers.containsKey(alt3) && suggestions.size() < 3) suggestions.add(alt3);
            if (!registeredUsers.containsKey(alt2) && suggestions.size() < 3) suggestions.add(alt2);
            counter++;
        }
        return suggestions;
    }

    public String getMostAttempted() {
        return attemptFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.comparing(AtomicInteger::get)))
                .map(e -> e.getKey() + " (" + e.getValue().get() + " attempts)")
                .orElse("None");
    }

    public static void main(String[] args) {
        UsernameChecker system = new UsernameChecker();
        
        // Setup initial state
        system.registerUser("john_doe", "user1");
        
        // Simulate high attempt frequency for 'admin'
        for(int i=0; i<10543; i++) system.checkAvailability("admin");

        System.out.println("checkAvailability(\"john_doe\") -> " + system.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " + system.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") -> " + system.suggestAlternatives("john_doe"));
        System.out.println("getMostAttempted() -> " + system.getMostAttempted());
    }
}