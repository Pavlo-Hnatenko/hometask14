import entity.Location;
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
                    log.info("inserted new contact. ID : {}", generatedKeys.getLong("id"));
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