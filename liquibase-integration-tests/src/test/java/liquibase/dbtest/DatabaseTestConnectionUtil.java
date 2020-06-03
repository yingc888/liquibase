package liquibase.dbtest;

import liquibase.CatalogAndSchema;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.test.DatabaseTestContext;
import liquibase.test.DatabaseTestURL;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTestConnectionUtil {
    private static Logger logger = LogService.getLog(DatabaseTestConnectionUtil.class);
    private static List<String> emptySchemas = new ArrayList<>();

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
    /**
     * Wipes all Liquibase schemas in the database before testing starts. This includes the DATABASECHANGELOG/LOCK
     * tables.
     */
    protected static void wipeDatabase(Database database) {
        emptySchemas.clear();
        try {
            // Try to erase the DATABASECHANGELOGLOCK (not: -LOG!) table that might be a leftover from a previously
            // crashed or interrupted integration test.
            // TODO the cleaner solution would be to have a noCachingHasObject() Method in SnapshotGeneratorFactory
            try {
                if (database.getConnection() != null) {
                    String sql = "DROP TABLE " + database.getDatabaseChangeLogLockTableName();
                    LogService.getLog(DatabaseTestConnectionUtil.class).info(LogType.WRITE_SQL, sql);
                    ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                            sql
                    );
                    database.commit();
                }
            } catch (SQLException e) {
                if (database instanceof PostgresDatabase) { // throws "current transaction is aborted" unless we roll back the connection
                    database.rollback();
                }
            }

            SnapshotGeneratorFactory.resetAll();
            emptyTestSchema(CatalogAndSchema.DEFAULT.getCatalogName(), CatalogAndSchema.DEFAULT.getSchemaName(),
                    database);
            SnapshotGeneratorFactory factory = SnapshotGeneratorFactory.getInstance();

            if (database.supportsSchemas()) {
                emptyTestSchema(null, DatabaseTestContext.ALT_SCHEMA, database);
            }
            if (supportsAltCatalogTests(database)) {
                if (database.supportsSchemas() && database.supportsCatalogs()) {
                    emptyTestSchema(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA, database);
                }
            }

            /*
             * There is a special treatment for identifiers in the case when (a) the RDBMS does NOT support
             * schemas AND (b) the RDBMS DOES support catalogs AND (c) someone uses "schemaName=..." in a
             * Liquibase ChangeSet. In this case, AbstractJdbcDatabase.escapeObjectName assumes the author
             * was intending to write "catalog=..." and transparently rewrites the expression.
             * For us, this means that we have to wipe both ALT_SCHEMA and ALT_CATALOG to be sure we
             * are doing a thorough cleanup.
             */
            CatalogAndSchema[] alternativeLocations = new CatalogAndSchema[]{
                    new CatalogAndSchema(DatabaseTestContext.ALT_CATALOG, null),
                    new CatalogAndSchema(null, DatabaseTestContext.ALT_SCHEMA),
                    new CatalogAndSchema("LBCAT2", database.getDefaultSchemaName()),
                    new CatalogAndSchema(null, "LBCAT2"),
                    new CatalogAndSchema("lbcat2", database.getDefaultSchemaName()),
                    new CatalogAndSchema(null, "lbcat2")
            };
            for (CatalogAndSchema location : alternativeLocations) {
                emptyTestSchema(location.getCatalogName(), location.getSchemaName(), database);
            }

            database.commit();
            SnapshotGeneratorFactory.resetAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms a given combination of catalogName and schemaName into the standardized format for the given
     * database. If the database has the
     *
     * @param catalogName catalog name (or null)
     * @param schemaName  schema name (or null)
     * @param database    the database where the target might exist
     * @throws LiquibaseException if any problem occurs during the process
     */
    private static void emptyTestSchema(String catalogName, String schemaName, Database database)
            throws LiquibaseException {
        SnapshotGeneratorFactory factory = SnapshotGeneratorFactory.getInstance();

        CatalogAndSchema target = new CatalogAndSchema(catalogName, schemaName).standardize(database);
        Schema schema = new Schema(target.getCatalogName(), target.getSchemaName());
        if (factory.has(schema, database)) {
            if (!emptySchemas.contains(target.toString())) {
                database.dropDatabaseObjects(target);
                emptySchemas.add(target.toString());
            }
        }

    }

    private static boolean supportsAltCatalogTests(Database database) {
        return database.supportsCatalogs();
    }
}
