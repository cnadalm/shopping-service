package cnm.tanger46.shopping.entity;

import java.time.Instant;
import java.util.UUID;

// public record ShoppingItem(String id, String label, Instant createdAt) {

//     public ShoppingItem(String label) {
//         this(UUID.randomUUID().toString(), label, Instant.now());
//     }

// }

public class ShoppingItem {

    private final String id;
    private final String label;
    private final Instant createdAt;

    public ShoppingItem(String label) {
        this.id = UUID.randomUUID().toString();
        this.label= label;
        this.createdAt = Instant.now();
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public Instant createdAt() {
        return createdAt;
    }

}
