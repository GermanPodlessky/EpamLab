package by.bsuir.implementation;

import by.bsuir.dao.ClientSqLMigration;
import by.bsuir.dao.DaoClient;
import by.bsuir.parser.ClientXmlParser;
import by.bsuir.service.ClientMigrationService;
import by.bsuir.service.ClientService;
import by.bsuir.service.ServiceException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The type Main.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ClientXmlParser clientParser = new ClientXmlParser("D:\\learn\\Epam\\EpamLab1\\src\\main\\resources\\client.xml", "D:\\learn\\Epam\\EpamLab1\\src\\main\\resources\\client.xsd");
        ClientService clientService = new ClientService(new DaoClient(clientParser));
        ClientSqLMigration clientSqLMigration = null;

        try {
            clientSqLMigration = new ClientSqLMigration();
            clientSqLMigration.setConnection(getConnection());
            var clientMigrationService = new ClientMigrationService(clientService, clientSqLMigration);
            clientMigrationService.Migrate();
        } catch (ServiceException | SQLException | IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException, IOException {
        var props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("database.properties"))) {
            props.load(in);
        }
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        return DriverManager.getConnection(url, username, password);
    }
}
