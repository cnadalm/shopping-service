package cnm.tanger46.shopping.boundary;

import java.lang.System.Logger;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;

import cnm.tanger46.shopping.entity.RepositoryException;
import cnm.tanger46.shopping.entity.ShoppingItem;
import cnm.tanger46.shopping.entity.ShoppingItemsRepository;
import io.helidon.common.http.Http;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class ShoppingItemsResource implements Service {

    private static final Logger LOGGER = System.getLogger(ShoppingItemsResource.class.getName());
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final String ERROR_MSG = "Something went wrong ...";

    private final ShoppingItemsRepository repository;

    public ShoppingItemsResource(final ShoppingItemsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/", this::allShoppingItemsHandler)
            .post("/", this::createShoppingItemHandler)
            .delete("/{id}", this::deleteShoppingItemHandler);
    }

    private void allShoppingItemsHandler(final ServerRequest request, final ServerResponse response) {
        try {
            final var jsonBuilder = JSON.createArrayBuilder();
            repository.all().stream()
                    .map(shoppingItem -> JSON.createObjectBuilder()
                        .add("id", shoppingItem.id())
                        .add("label", shoppingItem.label())
                        .add("createdAt", shoppingItem.createdAt().toString())
                        .build())
                    .forEach(jsonBuilder::add);
            response.send(jsonBuilder.build());
        } catch (RepositoryException e) {
            LOGGER.log(System.Logger.Level.ERROR, ERROR_MSG, e);
            response.send(e);
        }
    }

    private void createShoppingItemHandler(final ServerRequest request, final ServerResponse response) {
        request.content().as(JsonObject.class)
            .thenAccept(jsonRequest -> createShoppingItem(jsonRequest, response))
            .exceptionally(ex -> processErrors(ex, response));
    }

    private void createShoppingItem(final JsonObject jsonRequest, final ServerResponse response) {
        if (!jsonRequest.containsKey("label")) {
            final JsonObject jsonError = JSON.createObjectBuilder().add("error", "No label provided").build();
            response.status(Http.Status.BAD_REQUEST_400).send(jsonError);
            return;
        }

        try {
            final String label = jsonRequest.getString("label");
            repository.create(new ShoppingItem(label));
            response.status(Http.Status.NO_CONTENT_204).send();
        } catch (RepositoryException e) {
            LOGGER.log(System.Logger.Level.ERROR, ERROR_MSG, e);
            response.send(e);
        }
    }

    private void deleteShoppingItemHandler(final ServerRequest request, final ServerResponse response) {
        try {
            final var id = request.path().param("id");
            repository.delete(id);
            response.status(Http.Status.NO_CONTENT_204).send();
        } catch (RepositoryException e) {
            LOGGER.log(System.Logger.Level.ERROR, ERROR_MSG, e);
            response.send(e);
        }
    }

    private static <T> T processErrors(final Throwable ex, final ServerResponse response) {
        if (ex.getCause() instanceof JsonException) {
            LOGGER.log(System.Logger.Level.ERROR, "Invalid JSON", ex);
            JsonObject jsonErrorObject = JSON.createObjectBuilder().add("error", "Invalid JSON").build();
            response.status(Http.Status.BAD_REQUEST_400).send(jsonErrorObject);
        } else {
            LOGGER.log(System.Logger.Level.ERROR, "Internal error", ex);
            JsonObject jsonErrorObject = JSON.createObjectBuilder().add("error", "Internal error").build();
            response.status(Http.Status.INTERNAL_SERVER_ERROR_500).send(jsonErrorObject);
        }
        return null;
    }
}