package liquibase.utils

import liquibase.util.StringUtils
import org.yaml.snakeyaml.Yaml

class TestData {

  /**
   *
   * Map of {database : list of tests}
   * database : name of the database the test is defined for (or empty string if not defined)
   * test: map of key value pairs: descriptions, database, expected_sql etc
   * Note: we add the test_section to each test (the main key in the yaml)
   */
  private Map<String, List<Map<String, Object>>> contents = new HashMap<>()


  TestData(String filename) {
    Yaml yaml = new Yaml()
    // this map is not the same as the "contents" map
    Map<String, List<Map<String, Object>>> loadedContents = (Map<String, List>) yaml.load(FileUtils.getResourceFileContent(filename))
    loadedContents.each { section, tests ->
      tests.each { test ->
        test.put("test_section", section);
        String databases = test.get("database")
        if (StringUtils.isEmpty(databases)) {
          List<Map<String, Object>> contentTests = contents.get("")
          addToListInMap(contents,"", test)
        } else {
          databases?.split(",")*.trim().each { database ->
            test.put("test_valid_for_dbs", databases)
            addToListInMap(contents, database.toLowerCase(), test)
          }
        }
      }
    }
  }

  private void addToListInMap(Map<String, ?> map, String key, Object value) {
    List existingList = map.get(key)
    if (existingList == null) {
      existingList = new ArrayList()
      map.put(key, existingList)
    }
    existingList.add(value)
  }

  ExampleList forAllDatabases(String... keys) {
    return forDatabases(null, keys)
  }

  ExampleList forDatabases(List<String> databases = null, String... keys = null) {
    List<Map> testsForAskedDatabases = []
    contents.each { db, val ->
      if (shouldReturnForDb(db, databases)) {
        val.each { test  ->
          if (keys == null
              || keys.length == 0
              || keys*.toLowerCase().contains(test.get("test_section").toString().toLowerCase())) {
            Map testClone = test.clone()
            testClone.put("database", db)
            testsForAskedDatabases.add(testClone)
          }
        }
      }
    }
    return new ExampleList(testsForAskedDatabases)
  }

  /**
   * Returns true if:
   *  databases (running tests for) is null/empty or contains the db
   *  db (the test is marked for): is null or empty or the databases we are running for contains it
   * @param db Database value from the test (the test is marked for)
   * @param databases databases that we are running tests for
   * @return
   */
  boolean shouldReturnForDb(String db, List<String> databases) {
    return db == null || db.size() == 0 || databases == null || databases.isEmpty() || databases*.toLowerCase().contains(db)
  }


  String toYaml() {
    Yaml yaml = new Yaml()
    StringWriter writer = new StringWriter()
    yaml.dump(this.dataMap, writer)
    return writer.toString()
  }

  String toYaml(String... keys) {
    Map<String, List> filteredDataMap = new HashMap<>()
    for (String key : keys) {
      filteredDataMap.put(key, dataMap.get(key))
    }
    Yaml yaml = new Yaml()
    StringWriter writer = new StringWriter()
    yaml.dump(filteredDataMap, writer)
    return writer.toString()
  }

  void toFile(String filename) {
    FileUtils.writeToFile(filename, toYaml())
  }

  void toFile(String filename, String... keys) {
    FileUtils.writeToFile(filename, toYaml(keys))
  }

  // Our own list container for custom methods and easier chaining
  class ExampleList {
    private List<Map> examples

    ExampleList(List<Map> examples) {
      this.examples = examples
    }

    // This is mainly for providing test data in a way that Spock can consume.
    // It needs a list of lists to work properly
    List<List> properties(String... propertyKeys) {
      List<List> props = new ArrayList<>()
      for (Map example : this.examples) {
        props.add(getExampleProperties(example, propertyKeys))
      }
      return props
    }

    // Get a list of values from an example based on given keys.
    // This is mainly a helper method for getProps() to provide Spock data that it likes.
    private List getExampleProperties(Map example, String... propertyKeys) {
      List propsList = new ArrayList<>()
      for (String key : propertyKeys) {
        def value = example.get(key)
        propsList.add(value)
      }
      return propsList
    }
  }
}
