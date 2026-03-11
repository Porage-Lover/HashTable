import java.util.*;

public class PlagiarismDetector {
    private final Map<Integer, Set<String>> nGramIndex = new HashMap<>();
    private final Map<String, Integer> documentSize = new HashMap<>();
    private final int N = 3; // Reduced to 3-gram for this small demo

    public void addDocument(String docId, String text) {
        List<Integer> nGrams = extractNGrams(text);
        documentSize.put(docId, nGrams.size());
        for (int hash : nGrams) {
            nGramIndex.computeIfAbsent(hash, k -> new HashSet<>()).add(docId);
        }
    }

    public String analyzeDocument(String targetDocId, String text) {
        List<Integer> targetNGrams = extractNGrams(text);
        Map<String, Integer> matchCounts = new HashMap<>();

        for (int hash : targetNGrams) {
            Set<String> docs = nGramIndex.getOrDefault(hash, Collections.emptySet());
            for (String docId : docs) {
                if (!docId.equals(targetDocId)) {
                    matchCounts.put(docId, matchCounts.getOrDefault(docId, 0) + 1);
                }
            }
        }

        StringBuilder result = new StringBuilder("analyzeDocument(\"" + targetDocId + "\")\n");
        result.append("-> Extracted ").append(targetNGrams.size()).append(" n-grams\n");
        
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            double similarity = (double) entry.getValue() / targetNGrams.size() * 100;
            String flag = similarity > 50 ? " (PLAGIARISM DETECTED)" : " (suspicious)";
            result.append(String.format("-> Found %d matching n-grams with \"%s\"\n-> Similarity: %.1f%%%s\n", 
                entry.getValue(), entry.getKey(), similarity, flag));
        }
        return result.toString();
    }

    private List<Integer> extractNGrams(String text) {
        String[] words = text.toLowerCase().split("\\W+");
        List<Integer> hashes = new ArrayList<>();
        if (words.length < N) return hashes;
        
        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < N; j++) ngram.append(words[i + j]).append(" ");
            hashes.add(ngram.toString().trim().hashCode());
        }
        return hashes;
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();
        
        // Setup existing database
        detector.addDocument("essay_089.txt", "The quick brown fox jumps over the lazy dog. This is a common typing test.");
        detector.addDocument("essay_092.txt", "Artificial intelligence is the simulation of human intelligence processes by machines, especially computer systems.");
        
        // Document to analyze
        String suspiciousDoc = "Artificial intelligence is the simulation of human intelligence processes by machines. The quick brown fox is fast.";
        System.out.println(detector.analyzeDocument("essay_123.txt", suspiciousDoc));
    }
}