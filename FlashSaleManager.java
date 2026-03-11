import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleManager {
    private final Map<String, AtomicInteger> inventory = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentLinkedQueue<String>> waitingLists = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingLists.put(productId, new ConcurrentLinkedQueue<>());
    }

    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, new AtomicInteger(0)).get();
    }

    public String purchaseItem(String productId, String userId) {
        AtomicInteger stock = inventory.get(productId);
        if (stock != null) {
            while (true) {
                int currentStock = stock.get();
                if (currentStock <= 0) break;
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            }
        }
        
        ConcurrentLinkedQueue<String> waitlist = waitingLists.computeIfAbsent(productId, k -> new ConcurrentLinkedQueue<>());
        waitlist.offer(userId);
        return "Added to waiting list, position #" + waitlist.size();
    }

    public static void main(String[] args) {
        FlashSaleManager store = new FlashSaleManager();
        store.addProduct("IPHONE15_256GB", 100);

        System.out.println("checkStock(\"IPHONE15_256GB\") -> " + store.checkStock("IPHONE15_256GB") + " units available");
        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=12345) -> " + store.purchaseItem("IPHONE15_256GB", "12345"));
        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=67890) -> " + store.purchaseItem("IPHONE15_256GB", "67890"));
        
        System.out.println("... (simulating 98 more purchases) ...");
        for(int i = 0; i < 98; i++) {
            store.purchaseItem("IPHONE15_256GB", "user" + i);
        }

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=99999) -> " + store.purchaseItem("IPHONE15_256GB", "99999"));
    }
}