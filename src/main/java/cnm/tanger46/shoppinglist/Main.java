package cnm.tanger46.shoppinglist;

import cnm.tanger46.shoppinglist.boundary.api.v1.ShoppingListResource;
import cnm.tanger46.shoppinglist.boundary.storage.FileShoppingListRepository;
import cnm.tanger46.shoppinglist.control.ShoppingListService;
import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.cors.CrossOriginConfig;

/**
 * The application main class.
 */
public final class Main {

    private Main() {
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        startServer();
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     */
    static Single<WebServer> startServer() {

        // load logging configuration
        LogConfig.configureRuntime();

        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();

        WebServer server = WebServer.builder(createRouting(config))
                .config(config.get("server"))
                .addMediaSupport(JsonpSupport.create())
                .build();

        Single<WebServer> webserver = server.start();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        webserver.thenAccept(ws -> {
                    System.out.println("WEB server is up! http://localhost:" + ws.port() + "/api/shopping-items");
                    ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionallyAccept(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                });

        return webserver;
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return routing configured with JSON support, a health check, and a service
     * @param config configuration of this server
     */
    private static Routing createRouting(final Config config) {

        final var shoppingListRepository = new FileShoppingListRepository(config);
        final var shoppingListService = new ShoppingListService(shoppingListRepository);
        final var shoppingListResource = new ShoppingListResource(shoppingListService);

        final var metrics = MetricsSupport.create();
        final var health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();

        final var corsSupport = CorsSupport.builder()
                .addCrossOrigin(CrossOriginConfig.create()) 
                .build();

        return Routing.builder()
                .register(health)                   // Health at "/health"
                .register(metrics)                  // Metrics at "/metrics"
                .register("/shopping-list/api/items", corsSupport, shoppingListResource)
                .build();
    }
}
