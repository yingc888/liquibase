package liquibase.dbtest;

import liquibase.CatalogAndSchema;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.test.DatabaseTestContext;
import liquibase.test.DatabaseTestURL;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTestConnectionUtil {
    private static Logger logger = LogService.getLog(DatabaseTestConnectionUtil.class);

    public static Database initializeDatabase(Database database) throws DatabaseException {
        DatabaseTestURL testUrl = AbstractIntegrationTest.getDatabaseTestURL(database.getShortName());
        try {
            database = openConnection(testUrl.getUrl(), testUrl.getUsername(), testUrl.getPassword());
        }
        catch (Exception e) {
            logger.severe("Unable to initialize database connection", e);
            database = null;
            return database;
        }
        init(database);
        return database;
    }

    private static Database openConnection(String url, String username, String password) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url, username, password);

        Database database = null;
        if (connection != null) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        }
        return database;
    }

    private static void init(Database database) throws DatabaseException {
        if (database == null) {
            return;
        }

        if (database.supportsTablespaces()) {
            // Use the opportunity to test if the DATABASECHANGELOG table is placed in the correct tablespace
            database.setLiquibaseTablespaceName(DatabaseTestContext.ALT_TABLESPACE);
        }
        if (!database.getConnection().getAutoCommit()) {
            database.rollback();
        }

        // If we should test with a custom defaultSchemaName:
        // if (getDefaultSchemaName() != null && getDefaultSchemaName().length() > 0) {
        //     database.setDefaultSchemaName(getDefaultSchemaName());
        // }

        SnapshotGeneratorFactory.resetAll();
        ExecutorService.getInstance().reset();

        LockServiceFactory.getInstance().resetAll();
        LockServiceFactory.getInstance().getLockService(database).init();

        ChangeLogHistoryServiceFactory.getInstance().resetAll();
    }

}
