package cnm.tanger46.shoppinglist.entity;

import java.util.Collection;
import java.util.Optional;

public interface ShoppingListRepository {

    Collection<Item> all() throws RepositoryException;

    Optional<Item> find(final String id) throws RepositoryException;

    Item create(final Item shoppingItem) throws RepositoryException;

    Item update(final Item shoppingItem) throws RepositoryException;

    void delete(final String id) throws RepositoryException;

}
