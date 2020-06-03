package liquibase

import ch.qos.logback.classic.Level
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.core.SnapshotCommand
import liquibase.database.Database
import liquibase.database.DatabaseFactory
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static org.junit.Assert.assertThat;

class HelloTest extends Specification {
  Logger logger = LoggerFactory.getLogger(HelloTest.class)

  private String username
  private String password
  private String url

  static {
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.setLevel(Level.INFO)
  }


  //@Ignore
  @Unroll("#featureName: #description")
  def "test generate sql and verify object creation"() {
    given:
    Database database = DatabaseFactory.getInstance().getDatabase(dbms)
    database = DatabaseTestConnectionUtil.initializeDatabase(database)
    // Do not count the test as successful if we skip it because of a failed login. Count it as skipped instead.
    logger.info("Trying to run against database: {}", { database?.shortName })
    org.junit.Assume.assumeTrue(database != null)
    File tempChangelogFile = createChangeLogTempFile(description, changeset)
    String contexts = "test, context-b"
    Liquibase liquibase = TestUtils.createLiquibase(tempChangelogFile.getAbsolutePath(), database)
    //liquibase.setChangeLogParameter("loginuser", database.getConnection().getConnectionUserName())

    DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog()
    List<ChangeSet> changeSets = changeLog.getChangeSets()

    if (expected_sql) {
      ArrayList<String> expectedSqlList = collectValuesForDb(expected_sql, dbms)
      when:
      List<String> generatedSql = TestUtils.toSqlFromChangeSets(changeSets, database)
      then:
      assertThat(expectedSqlList, containsInAnyOrder(generatedSql.toArray()))
    }

    if (!StringUtils.isEmpty(expected_snapshot)) {
      when:
      ArrayList<String> snapshotSchemas = collectValuesForDb(snapshot_schema, dbms, ",")
      List<CatalogAndSchema> catalogAndSchemaList = getCatalogAndSchema(snapshotSchemas, database)
      catalogAndSchemaList.each { database.dropDatabaseObjects(it) }
      DatabaseTestConnectionUtil.wipeDatabase(database);
      runUpdate(liquibase, contexts)
      String jsonSnapshot = getJsonSnapshot(database, catalogAndSchemaList)
      logger.info(jsonSnapshot)
      //catalogAndSchemaList.each {database.dropDatabaseObjects(it)}

      then:
      snapshotMatchesSpecifiedStructure(expected_snapshot, jsonSnapshot)
    }

    where:
    [description, dbms, changeset, expected_sql, expected_snapshot, snapshot_schema] <<
        new TestData("tests/master.yml")
            .forDatabases(["postgresql"/*, "mysql"*/], "TABLE")
        //.forAllDatabases("TABLE")
        //.forAllDatabases()
            .properties("description", "database", "changeset", "expected_sql", "expected_snapshot", "snapshot_schema")
            .iterator()

  }

  private ArrayList<String> collectValuesForDb(Object value, String dbms, String splitWith = null) {
    List<String> returnList = new ArrayList<>()
    if (!value) {
      return returnList
    }
    if (value instanceof Map) {
      def valueForDB = value.get(dbms)
      if (valueForDB) {
        returnList.addAll(splitAndTrimIfNeeded(valueForDB, splitWith))
      }
    } else {
      returnList.addAll(splitAndTrimIfNeeded(value, splitWith))
    }
    return returnList
  }

  private List<String> splitAndTrimIfNeeded(String str, String regex = null) {
    List<String> returnList = new ArrayList<>()
    if (str) {
      if (regex == null) {
        returnList.add(str)
        return returnList
      }
      return str?.split(regex)*.trim()
    }
    return new ArrayList<String>()
  }

  private Collection<String> splitAndTrimIfNeeded(Collection<String> strs, String regex = null) {
    if (regex == null) {
      return strs
    }
    List<String> returnList = new ArrayList<>()
    strs.each { str ->
      if (str) {
        returnList.add(str.split(regex)*.trim())
      }
    }
    return returnList
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

  private ArrayList<CatalogAndSchema> getCatalogAndSchema(List<String> schemaList, Database database) {
    List<CatalogAndSchema> finalList = new ArrayList<>()
    schemaList?.each { sch ->
      String[] catSchema = sch.split("\\.")
      String catalog, schema
      if (catSchema.length == 2) {
        catalog = catSchema[0]?.trim()
        schema = catSchema[1]?.trim()
      } else if (catSchema.length == 1) {
        catalog = null
        schema = catSchema[0]?.trim()
      } else {
        return finalList
      }
      finalList.add(new CatalogAndSchema(catalog, schema).customize(database))
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

      @Override
      void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        if (expectedValue instanceof String && actualValue instanceof String) {
          if (!StringUtils.equalsIgnoreCaseAndEmpty(expectedValue, actualValue)) {
            result.fail(prefix, expectedValue, actualValue);
          }
        } else {
          super.compareValues(prefix, expectedValue, actualValue, result)
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
