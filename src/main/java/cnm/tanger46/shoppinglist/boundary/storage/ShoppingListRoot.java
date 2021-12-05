package cnm.tanger46.shoppinglist.boundary.storage;

import java.util.HashMap;
import java.util.Map;

import cnm.tanger46.shoppinglist.entity.Item;

public class ShoppingListRoot {

    private final Map<String, Item> items = new HashMap<>();

    public Map<String, Item> items() {
        return items;
    }

}
