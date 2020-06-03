package liquibase.utils

import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.executor.ExecutorService
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.test.JUnitResourceAccessor

class TestUtils {

  static Liquibase createLiquibase(String changeLogFile, Database database) throws Exception {
    CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(new JUnitResourceAccessor(), new FileSystemResourceAccessor());
    return createLiquibase(changeLogFile, fileOpener, database);
  }

  static Liquibase createLiquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database) {
    ExecutorService.getInstance().clearExecutor(database);
    database.resetInternalState();
    return new Liquibase(changeLogFile, resourceAccessor, database);
  }

  static List<String> toSql(Change change, Database db) {
    Sql[] sqls = SqlGeneratorFactory.newInstance().generateSql(change, db)
    return sqls*.toSql()
  }

  static List<String> toSql(ChangeSet changeSet, Database db) {
    return toSql(changeSet.getChanges(), db)
  }

  static List<String> toSql(List<? extends Change> changes, Database db) {
    List<String> stringList = new ArrayList<>()
    changes.each { stringList.addAll(toSql(it, db)) }
    return stringList
  }

  static List<String> toSqlFromChangeSets(List<ChangeSet> changeSets, Database db) {
    List<String> stringList = new ArrayList<>()
    changeSets.each { stringList.addAll(toSql(it, db)) }
    return stringList
  }


}
