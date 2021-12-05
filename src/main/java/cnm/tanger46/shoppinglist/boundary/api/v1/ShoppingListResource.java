package cnm.tanger46.shoppinglist.boundary.api.v1;

import java.lang.System.Logger;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;

import cnm.tanger46.shoppinglist.control.CreateItemRequest;
import cnm.tanger46.shoppinglist.control.DeleteItemRequest;
import cnm.tanger46.shoppinglist.control.ServiceException;
import cnm.tanger46.shoppinglist.control.ShoppingListService;
import io.helidon.common.http.Http;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class ShoppingListResource implements Service {

    private static final Logger LOGGER = System.getLogger(ShoppingListResource.class.getName());
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final String ERROR_MSG = "Something went wrong ...";

    private final ShoppingListService service;

    public ShoppingListResource(final ShoppingListService service) {
        this.service = service;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/", this::allItemsHandler)
            .post("/", this::createItemHandler)
            .delete("/{id}", this::deleteItemHandler);
    }

    private void allItemsHandler(final ServerRequest request, final ServerResponse response) {
        try {
            final var jsonBuilder = JSON.createArrayBuilder();
            service.allItems().items().stream()
                    .map(shoppingItem -> JSON.createObjectBuilder()
                        .add("id", shoppingItem.id())
                        .add("label", shoppingItem.label())
                        .add("createdAt", shoppingItem.createdAt().toString())
                        .build())
                    .forEach(jsonBuilder::add);
            response.send(jsonBuilder.build());
        } catch (ServiceException e) {
            LOGGER.log(System.Logger.Level.ERROR, ERROR_MSG, e);
            response.send(e);
        }
    }

    private void createItemHandler(final ServerRequest request, final ServerResponse response) {
        request.content().as(JsonObject.class)
            .thenAccept(jsonRequest -> createItem(jsonRequest, response))
            .exceptionally(ex -> processErrors(ex, response));
    }

    private void createItem(final JsonObject jsonRequest, final ServerResponse response) {
        if (!jsonRequest.containsKey("label")) {
            final JsonObject jsonError = JSON.createObjectBuilder().add("error", "No label provided").build();
            response.status(Http.Status.BAD_REQUEST_400).send(jsonError);
            return;
        }

        try {
            final String label = jsonRequest.getString("label");
            service.createItem(new CreateItemRequest(label));
            response.status(Http.Status.NO_CONTENT_204).send();
        } catch (ServiceException e) {
            LOGGER.log(System.Logger.Level.ERROR, ERROR_MSG, e);
            response.send(e);
        }
    }

    private void deleteItemHandler(final ServerRequest request, final ServerResponse response) {
        try {
            final var id = request.path().param("id");
            service.deleteItem(new DeleteItemRequest(id));
            response.status(Http.Status.NO_CONTENT_204).send();
        } catch (ServiceException e) {
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