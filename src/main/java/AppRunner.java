import entity.Location;
import entity.Problem;
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
    static Properties props = loadProperties();
    static String url = props.getProperty("url");
    static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(url, props);
            log.info("Connecting to {}", url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

            addLocations();
            addRoutes();
            addProblems();
            setSolutions(getCostSet());

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

    private static void addLocations() {

        Location[] locations = {
                new Location("gdansk"),
                new Location("bydgoszcz"),
                new Location("torun"),
                new Location("warszawa")
        };

        try (PreparedStatement insertLocation = connection.prepareStatement(
                "INSERT INTO locations (name) VALUES (?) ON CONFLICT DO NOTHING;"
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addRoutes() {

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
                "INSERT INTO routes (from_id, to_id, cost) VALUES (?, ?, ?) ON CONFLICT DO NOTHING;"
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addProblems() {

        Problem[] problems = {
                new Problem(1, 4),
                new Problem(2, 4)
        };

        try (PreparedStatement insertProblem = connection.prepareStatement(
                "INSERT INTO problems (from_id, to_id) VALUES (?, ?) ON CONFLICT DO NOTHING;"
        )) {

            for (Problem problem : problems) {
                insertProblem.setInt(1, problem.getFromId());
                insertProblem.setInt(2, problem.getToId());

                insertProblem.addBatch();
            }

            insertProblem.executeBatch();
            ResultSet generatedKeys = insertProblem.getGeneratedKeys();

            while (generatedKeys.next()) {
                log.info("inserted new problem. ID : {}", generatedKeys.getLong("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ResultSet getCostSet() {

        ResultSet costSet = null;

        try (PreparedStatement selectProblem = connection.prepareStatement(
                "SELECT * FROM problems"
        )) {

            ResultSet problemSet = selectProblem.executeQuery();

            while (problemSet.next()) {

                int fromId = problemSet.getInt("from_id");
                int toId = problemSet.getInt("to_id");

                try (PreparedStatement findRoutes = connection.prepareStatement(
                        "SELECT cost FROM routes WHERE from_id = ? AND to_id = ? ON CONFLICT DO NOTHING;"
                )) {

                    findRoutes.setInt(1, fromId);
                    findRoutes.setInt(2, toId);

                    findRoutes.addBatch();

                    costSet = findRoutes.executeQuery();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return costSet;
    }

    private static void setSolutions(ResultSet costSet) {

        while (true) {
            try {
                if (!costSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try (PreparedStatement insertSolutions = connection.prepareStatement(
                    "INSERT INTO solutions (cost) VALUES (?) ON CONFLICT DO NOTHING;"
            )) {
                insertSolutions.setInt(1, costSet.getInt("cost"));

                insertSolutions.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}