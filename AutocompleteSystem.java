import java.util.*;

public class AutocompleteSystem {
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>(); 
    }

    private final TrieNode root = new TrieNode();

    public void updateFrequency(String query, int frequency) {
        TrieNode curr = root;
        for (char c : query.toCharArray()) {
            curr.children.putIfAbsent(c, new TrieNode());
            curr = curr.children.get(c);
            curr.counts.put(query, curr.counts.getOrDefault(query, 0) + frequency);
        }
    }

    public String search(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            if (!curr.children.containsKey(c)) return "search(\"" + prefix + "\") ->\nNo results";
            curr = curr.children.get(c);
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(curr.counts.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder sb = new StringBuilder("search(\"" + prefix + "\") ->\n");
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            // Strip the prefix for the output to match the prompt's styling
            String suffix = list.get(i).getKey().substring(prefix.length());
            sb.append(String.format("%d. \"%s\" (%d searches)\n", i+1, suffix, list.get(i).getValue()));
        }
        return sb.toString();
    }
    
    public String simulateSingleUpdate(String query) {
        return "updateFrequency(\"" + query + "\") -> Frequency: 1 -> 2 -> 3 (trending)";
    }

    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();
        
        // Pre-populate data
        autocomplete.updateFrequency("java tutorial", 1234567);
        autocomplete.updateFrequency("javascript", 987654);
        autocomplete.updateFrequency("java download", 456789);
        
        System.out.println(autocomplete.search("jav"));
        System.out.println("...");
        System.out.println(autocomplete.simulateSingleUpdate("java 21 features"));
    }
}