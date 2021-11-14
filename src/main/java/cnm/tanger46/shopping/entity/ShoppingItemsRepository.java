package cnm.tanger46.shopping.entity;

import java.util.Collection;
import java.util.Optional;

public interface ShoppingItemsRepository {

    Collection<ShoppingItem> all() throws RepositoryException;

    Optional<ShoppingItem> find(final String id) throws RepositoryException;

    ShoppingItem create(final ShoppingItem shoppingItem) throws RepositoryException;

    ShoppingItem update(final ShoppingItem shoppingItem) throws RepositoryException;

    void delete(final String id) throws RepositoryException;

}
