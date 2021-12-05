package cnm.tanger46.shoppinglist.control;

import java.util.Collection;

import cnm.tanger46.shoppinglist.entity.Item;

public record AllItemsResponse(Collection<Item> items) {
    
}
