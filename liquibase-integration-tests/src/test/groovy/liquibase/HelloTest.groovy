package liquibase

import ch.qos.logback.classic.Level
import liquibase.database.Database
import liquibase.database.core.PostgresDatabase
import liquibase.util.StringUtils
import liquibase.dbtest.DatabaseTestConnectionUtil
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll

class HelloTest extends Specification {

  private Set<String> emptySchemas = new TreeSet<>();


  static {
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.INFO);
  }


  @Unroll
  def "test postgres"() {
    given:
    // Database database = null// properly initialized database
    Database database = new PostgresDatabase();
    database = DatabaseTestConnectionUtil.initializeDatabase(database)
    String changelogDir = "pgsql"
    String changelogPath = "changelogs/" + changelogDir + changelog


    when:
    List<String> generatedSql = generateSQL(changelogPath)

    then:
    expected_sql == generatedSql

    when:
    runChangeLogFile(changelogPath)
    String jsonSnapshot = getJsonSnapshot(database)

    then:
    if (StringUtils.isEmpty(expected_db)) {
      snapshotMatchesSpecifiedStructure(jsonSnapshot, expected_db);
    }

    where:
    changelog                      | expected_sql | expected_db
    "/complete/root.changelog.xml" | ["", ""]     | '{"table": {"name": "table1", "primaryKey": "PK1"}}'

//    changelogDir | changelog                      | expected_sql | expected_db
//    "pgsql"      | "/complete/root.changelog.xml" | ""           | ""
//    "pgsql"      | "/complete/root.changelog.xml" | ""           | ""
//    "pgsql"      | "/complete/root.changelog.xml" | ""           | ""
//    "pgsql"      | "/complete/root.changelog.xml" | ""           | ""
//    "pgsql"      | "/complete/root.changelog.xml" | ""           | ""
  }

  List<String> generateSQL(String s) {
    null
  }

  void runChangeLogFile(String s) {

  }

  String getJsonSnapshot(Database database) {
    null
  }

  void snapshotMatchesSpecifiedStructure(String s1, String s2) {

  }
}
