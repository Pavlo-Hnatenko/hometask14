import entity.Location;
import entity.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.*;
import java.util.Properties;

public class AppRunner {

    private static final Logger log = LoggerFactory.getLogger(AppRunner.class);

    public static void main(String[] args) {

        Properties props = loadProperties();
        String url = props.getProperty("url");
        log.info("Connecting to {}", url);

        try (Connection connection = DriverManager.getConnection(url, props)) {

            try (PreparedStatement getAllLocations = connection.prepareStatement(
                    "SELECT name FROM locations")) {

                ResultSet resultSet = getAllLocations.executeQuery();

                while (resultSet.next()) {

                    String name = resultSet.getString("name");
                    log.info("Location is founded. Name: {}", name);
                }
            }

            Location[] locations = {
                    new Location("gdansk"),
                    new Location("bydgoszcz"),
                    new Location("torun"),
                    new Location("warszawa")
            };

            try (PreparedStatement insertLocation = connection.prepareStatement(

                    "INSERT INTO locations (name) VALUES (?) ON CONFLICT DO NOTHING;",
                    PreparedStatement.RETURN_GENERATED_KEYS
            )) {

                for (Location location : locations) {
                    insertLocation.setString(1, location.getName());
                    insertLocation.addBatch();
                }

                insertLocation.executeBatch();
                ResultSet generatedKeys = insertLocation.getGeneratedKeys();

                while (generatedKeys.next()) {
                    log.info("inserted new location. ID : {}", generatedKeys.getLong("id"));
                }
            }

            Route[] routes = {
                    new Route(1, 2, 1),
                    new Route(1, 3, 3),
                    new Route(2, 1, 1),
                    new Route(2, 3, 1),
                    new Route(2, 4, 4),
                    new Route(3, 1, 3),
                    new Route(3, 2, 1),
                    new Route(3, 4, 1),
                    new Route(4, 2, 4),
                    new Route(4, 3, 1),

            };

            try (PreparedStatement insertRoute = connection.prepareStatement(

                    "INSERT INTO routes (from_id, to_id, cost) VALUES (?, ?, ?) ON CONFLICT DO NOTHING;",
                    PreparedStatement.RETURN_GENERATED_KEYS
            )) {

                for (Route route : routes) {
                    insertRoute.setInt(1, route.getFromId());
                    insertRoute.setInt(2, route.getToId());
                    insertRoute.setInt(3, route.getCost());

                    insertRoute.addBatch();
                }

                insertRoute.executeBatch();
                ResultSet generatedKeys = insertRoute.getGeneratedKeys();

                while (generatedKeys.next()) {
                    log.info("inserted new route. ID : {}", generatedKeys.getLong("id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties() {

        Properties props = new Properties();

        try (InputStream input = AppRunner.class.getResourceAsStream("postgresql.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return props;
    }

}