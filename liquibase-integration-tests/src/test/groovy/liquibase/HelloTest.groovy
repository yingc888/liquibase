package liquibase

import ch.qos.logback.classic.Level
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.core.SnapshotCommand
import liquibase.database.Database
import liquibase.database.core.PostgresDatabase
import liquibase.dbtest.DatabaseTestConnectionUtil
import liquibase.exception.ValidationFailedException
import liquibase.util.StringUtils
import liquibase.utils.TestData
import liquibase.utils.TestUtils
import org.json.JSONArray
import org.json.JSONException
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.skyscreamer.jsonassert.comparator.DefaultComparator
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll

class HelloTest extends Specification {

  private String username
  private String password
  private String url

  static {
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.setLevel(Level.INFO)
  }

/*  public void setup() {
    // Get the integration test properties for both global settings and (if applicable) local overrides.
    Properties integrationTestProperties
    integrationTestProperties = new Properties()
    integrationTestProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"))
    InputStream localProperties = Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties")
    if (localProperties != null)
      integrationTestProperties.load(localProperties)

    // Login username
    username = integrationTestProperties.getProperty("integration.test." + dbms.getShortName() + ".username")
    if (username == null)
      username = integrationTestProperties.getProperty("integration.test.username")


    // Login password
    password = integrationTestProperties.getProperty("integration.test." + dbms.getShortName() + ".password")
    if (password == null)
      password = integrationTestProperties.getProperty("integration.test.password")
  }*/

  //@Ignore
  @Unroll
  def "test generate sql and verify object creation"() {
    given:
    Database database = DatabaseTestConnectionUtil.initializeDatabase(new PostgresDatabase())
    // Do not count the test as successful if we skip it because of a failed login. Count it as skipped instead.
    org.junit.Assume.assumeTrue(database != null)
    File tempChangelogFile = createChangeLogTempFile(description, changeset)
    String contexts = "test, context-b"
    Liquibase liquibase = TestUtils.createLiquibase(tempChangelogFile.getAbsolutePath(), database)
    //liquibase.setChangeLogParameter("loginuser", database.getConnection().getConnectionUserName())

    DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog()
    List<ChangeSet> changeSets = changeLog.getChangeSets()

    if (!StringUtils.isEmpty(expected_sql)) {
      when:
      List<String> generatedSql = TestUtils.toSqlFromChangeSets(changeSets, database)
      then:
      expected_sql == generatedSql
    }

    if (!StringUtils.isEmpty(expected_snapshot)) {
      when:
      List<CatalogAndSchema> catalogAndSchemaList = getCatalogAndSchema(snapshot_schema, database)
      catalogAndSchemaList.each {database.dropDatabaseObjects(it)}
      runUpdate(liquibase, contexts)
      String jsonSnapshot = getJsonSnapshot(database, catalogAndSchemaList)
      println jsonSnapshot
      //catalogAndSchemaList.each {database.dropDatabaseObjects(it)}

      then:
      snapshotMatchesSpecifiedStructure(expected_snapshot, jsonSnapshot)
    }

    where:
    [description, changeset, expected_sql, expected_snapshot, snapshot_schema] <<
        new TestData("tests/master.yml")
            .examples()
            .properties("description", "changeset", "expected_sql", "expected_snapshot", "snapshot_schema")
            .iterator()

  }


  void runUpdate(Liquibase liquibase, String contexts) {
    try {
      liquibase.update(contexts)
    } catch (ValidationFailedException e) {
      e.printDescriptiveError(System.err)
      throw e
    }
  }

  String getJsonSnapshot(Database database, List<CatalogAndSchema> schemaList) {
    SnapshotCommand snapshotCommand = new SnapshotCommand()
    snapshotCommand.setDatabase(database)
    snapshotCommand.setSerializerFormat("json")
    if (!schemaList.isEmpty()) {
      snapshotCommand.setSchemas(schemaList.toArray(new CatalogAndSchema[schemaList.size()]))
    }
    SnapshotCommand.SnapshotCommandResult result = snapshotCommand.execute()
    return result.print()
  }

  private ArrayList<CatalogAndSchema> getCatalogAndSchema(String schemaList, Database database) {
    List<CatalogAndSchema> finalList = new ArrayList<>()
    schemaList.split("\\s*,\\s*").each { schema ->
      finalList.add(new CatalogAndSchema(null, schema).customize(database))
    }
    return finalList
  }

  void snapshotMatchesSpecifiedStructure(String expected, String actual) {

    JSONAssert.assertEquals(expected, actual, new DefaultComparator(JSONCompareMode.LENIENT) {

      @Override
      void compareJSONArray(String prefix, JSONArray exp, JSONArray act, JSONCompareResult result) throws JSONException {
        if (exp.length() != 0) {
          if (JSONCompareUtil.allSimpleValues(exp)) {
            this.compareJSONArrayOfSimpleValues(prefix, exp, act, result)
          } else if (JSONCompareUtil.allJSONObjects(exp)) {
            this.compareJSONArrayOfJsonObjects(prefix, exp, act, result)
          } else {
            this.recursivelyCompareJSONArray(prefix, exp, act, result)
          }

        }
      }
    })
  }


  private File createChangeLogTempFile(String testName, String changeSets) {
    String changeLog = """
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

    ${changeSets}

</databaseChangeLog>
"""
    File tempFile = File.createTempFile(testName, ".xml")
    tempFile.write(changeLog)
    return tempFile
  }

}
