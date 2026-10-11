
package com.sparrowwallet.sparrow.wallet.donation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DonationContactsMigrationTest {

    @Test
    void migrationCreatesDonationContactTable() throws Exception {

        String url =
                "jdbc:h2:mem:donationContactsTest;" +
                "DB_CLOSE_DELAY=-1;" +
                "DATABASE_TO_UPPER=false";

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations(
                    "filesystem:src/main/resources/com/sparrowwallet/sparrow/sql"
                )
                .schemas("wallet_master")
                .load();

        // Aplica les migracions V1, V2... V11
        flyway.migrate();

        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        try(Connection connection = DriverManager.getConnection(url, "sa", "");
            Statement statement = connection.createStatement()) {

            // La taula ha d'existir i permetre inserir contactes
            statement.executeUpdate(
                    "INSERT INTO wallet_master.donationContact " +
                    "(id, name, silentPaymentAddress) VALUES " +
                    "('" + id1 + "', 'Contact1', 'tsp1-test-address')"
            );

            // Comprovem que el contacte es pot recuperar
            try(ResultSet result = statement.executeQuery(
                    "SELECT name FROM wallet_master.donationContact " +
                    "WHERE id = '" + id1 + "'"
            )) {
                assertTrue(result.next());
                assertEquals("Contact1", result.getString("name"));
            }

            // Una adreça duplicada s'ha de rebutjar
            assertThrows(SQLException.class, () ->
                    statement.executeUpdate(
                            "INSERT INTO wallet_master.donationContact " +
                            "(id, name, silentPaymentAddress) VALUES " +
                            "('" + id2 + "', 'Contact2', 'tsp1-test-address')"
                    )
            );

            // Flyway ha de registrar que V11 s'ha aplicat
            try(ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) FROM wallet_master.flyway_schema_history " +
                    "WHERE version = '11' AND success = TRUE"
            )) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
        }

        // Tornar a migrar no ha d'esborrar les dades
        flyway.migrate();

        try(Connection connection = DriverManager.getConnection(url, "sa", "");
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) FROM wallet_master.donationContact"
            )) {

            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }
    }
}
