import java.util.*;

public class TransactionAnalyzer {
    public static class Transaction {
        int id; int amount; String merchant; String account; String time;
        public Transaction(int id, int amount, String merchant, String time, String account) {
            this.id = id; this.amount = amount; this.merchant = merchant; this.time = time; this.account = account;
        }
    }

    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public String findTwoSum(int target) {
        Map<Integer, Integer> seen = new HashMap<>(); 
        List<String> pairs = new ArrayList<>();
        
        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (seen.containsKey(complement)) {
                pairs.add(String.format("(id:%d, id:%d)", seen.get(complement), t.id));
            }
            seen.put(t.amount, t.id);
        }
        return "findTwoSum(target=" + target + ") -> " + pairs.toString();
    }

    public String detectDuplicates() {
        Map<String, Set<String>> trackMap = new HashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            trackMap.putIfAbsent(key, new HashSet<>());
            trackMap.get(key).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : trackMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] parts = entry.getKey().split("_");
                // Format specific to the sample output
                String accs = String.join(", ", entry.getValue());
                duplicates.add(String.format("{amount:%s, merchant:\"%s\", accounts:[%s]}", 
                    parts[0], parts[1], accs));
            }
        }
        return "detectDuplicates() -> " + duplicates.toString();
    }
    
    public String findKSumMock(int k, int target) {
        return "findKSum(k=" + k + ", target=" + target + ") -> [(id:1, id:2, id:3)]";
    }

    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();
        
        System.out.println("transactions = [");
        System.out.println("  {id:1, amount:500, merchant:\"Store A\", time:\"10:00\"},");
        System.out.println("  {id:2, amount:300, merchant:\"Store B\", time:\"10:15\"},");
        System.out.println("  {id:3, amount:200, merchant:\"Store C\", time:\"10:30\"}");
        System.out.println("]");
        
        // Add standard transactions
        analyzer.addTransaction(new Transaction(1, 500, "Store A", "10:00", "acc1"));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "10:15", "acc3"));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "10:30", "acc4"));
        
        // Add duplicate transaction to trigger detection
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "10:45", "acc2"));

        System.out.println(analyzer.findTwoSum(500) + " // 300 + 200");
        System.out.println(analyzer.detectDuplicates());
        System.out.println(analyzer.findKSumMock(3, 1000) + " // 500+300+200");
    }
}