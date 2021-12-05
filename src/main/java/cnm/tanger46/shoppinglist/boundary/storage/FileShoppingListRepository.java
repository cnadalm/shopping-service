package cnm.tanger46.shoppinglist.boundary.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cnm.tanger46.shoppinglist.entity.RepositoryException;
import cnm.tanger46.shoppinglist.entity.Item;
import cnm.tanger46.shoppinglist.entity.ShoppingListRepository;
import io.helidon.config.Config;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
// import one.microstream.storage.restservice.types.StorageRestService;
// import one.microstream.storage.restservice.types.StorageRestServiceResolver;

public class FileShoppingListRepository implements ShoppingListRepository {

    private final String storagePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    private EmbeddedStorageManager storage = null;
    // private StorageRestService restService = null;

    public FileShoppingListRepository(final Config config) {
        final var storageRelativePath = config.get("app.storage.path").asString().orElse("shopping-storage");
        this.storagePath = System.getProperty("user.dir") + File.separator + storageRelativePath;
    }

    @Override
    public Collection<Item> all() throws RepositoryException {
        lock.readLock().lock();
        try {
            return data().items().values();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Item> find(final String id) throws RepositoryException {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(data().items().get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Item create(final Item shoppingItem) throws RepositoryException {
        lock.writeLock().lock();
        try {
            data().items().put(shoppingItem.id(), shoppingItem);
            storage.storeAll(data().items());
            return shoppingItem;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Item update(final Item shoppingItem) throws RepositoryException {
        lock.writeLock().lock();
        try {
            this.delete(shoppingItem.id());
            this.create(shoppingItem);
            return shoppingItem;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(final String id) throws RepositoryException {
        lock.writeLock().lock();
        try {
            data().items().remove(id);
            storage.store(data().items());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ShoppingListRoot data() throws RepositoryException {
        if (Objects.isNull(storage)) {
            final var path = obtainStoragePath();
            this.storage = EmbeddedStorage.start(path);
            if (Objects.isNull(storage.root())) {
                storage.setRoot(new ShoppingListRoot());
                storage.storeRoot();
            }
            // create the REST service
            // restService = StorageRestServiceResolver.resolve(storage);
            // restService.start();
        }
        return (ShoppingListRoot) storage.root();
    }

    private Path obtainStoragePath() throws RepositoryException {
        final var path = Paths.get(storagePath);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
        return path;
    }

}
