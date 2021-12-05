package cnm.tanger46.shoppinglist.control;

import cnm.tanger46.shoppinglist.entity.RepositoryException;
import cnm.tanger46.shoppinglist.entity.Item;
import cnm.tanger46.shoppinglist.entity.ShoppingListRepository;

public class ShoppingListService {

    private final ShoppingListRepository repository;

    public ShoppingListService(final ShoppingListRepository repository) {
        this.repository = repository;
    }
    
    public AllItemsResponse allItems() throws ServiceException {
        try {
            return new AllItemsResponse(repository.all());
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    public CreateItemResponse createItem(final CreateItemRequest request) throws ServiceException {
        try {
            final Item shoppingItem = repository.create(new Item(request.label()));
            return new CreateItemResponse(shoppingItem.id());
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    public void deleteItem(final DeleteItemRequest request) throws ServiceException {
        try {
            repository.delete(request.id());
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }
    
}
