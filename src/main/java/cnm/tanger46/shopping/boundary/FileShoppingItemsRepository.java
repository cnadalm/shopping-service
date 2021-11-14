package cnm.tanger46.shopping.boundary;

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

import cnm.tanger46.shopping.entity.RepositoryException;
import cnm.tanger46.shopping.entity.ShoppingItem;
import cnm.tanger46.shopping.entity.ShoppingItemsRepository;
import io.helidon.config.Config;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
// import one.microstream.storage.restservice.types.StorageRestService;
// import one.microstream.storage.restservice.types.StorageRestServiceResolver;

public class FileShoppingItemsRepository implements ShoppingItemsRepository {

    private final String storagePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    private EmbeddedStorageManager storage = null;
    // private StorageRestService restService = null;

    public FileShoppingItemsRepository(final Config config) {
        final var storageRelativePath = config.get("app.storage.path").asString().orElse("shopping-storage");
        this.storagePath = System.getProperty("user.dir") + File.separator + storageRelativePath;
    }

    @Override
    public Collection<ShoppingItem> all() throws RepositoryException {
        lock.readLock().lock();
        try {
            return data().shoppingItems().values();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<ShoppingItem> find(final String id) throws RepositoryException {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(data().shoppingItems().get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ShoppingItem create(final ShoppingItem shoppingItem) throws RepositoryException {
        lock.writeLock().lock();
        try {
            data().shoppingItems().put(shoppingItem.id(), shoppingItem);
            storage.storeAll(data().shoppingItems());
            return shoppingItem;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ShoppingItem update(final ShoppingItem shoppingItem) throws RepositoryException {
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
            data().shoppingItems().remove(id);
            storage.store(data().shoppingItems());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ShoppingItemsRoot data() throws RepositoryException {
        if (Objects.isNull(storage)) {
            final var path = obtainStoragePath();
            this.storage = EmbeddedStorage.start(path);
            if (Objects.isNull(storage.root())) {
                storage.setRoot(new ShoppingItemsRoot());
                storage.storeRoot();
            }
            // create the REST service
            // restService = StorageRestServiceResolver.resolve(storage);
            // restService.start();
        }
        return (ShoppingItemsRoot) storage.root();
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
