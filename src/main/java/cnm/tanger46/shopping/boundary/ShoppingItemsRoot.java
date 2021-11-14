package cnm.tanger46.shopping.boundary;

import java.util.HashMap;
import java.util.Map;

import cnm.tanger46.shopping.entity.ShoppingItem;

public class ShoppingItemsRoot {

    private final Map<String, ShoppingItem> shoppingItems = new HashMap<>();

    public Map<String, ShoppingItem> shoppingItems() {
        return shoppingItems;
    }

}
