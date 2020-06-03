package liquibase.utils


import org.yaml.snakeyaml.Yaml

class TestData {

  private Map<String, List> dataMap

  TestData(String filename) {
    Yaml yaml = new Yaml()
    this.dataMap = (Map<String, List>) yaml.load(FileUtils.getResourceFileContent(filename))
  }


  ExampleList examples() {
    List<Map> examplesList = new ArrayList<>()
    for (String key : this.dataMap.keySet()) {
      examplesList.addAll(this.dataMap.get(key))
    }
    return new ExampleList(examplesList)
  }

  ExampleList examples(String... keys) {
    List<Map> examplesList = new ArrayList<>()
    for (String key : keys) {
      examplesList.addAll(this.dataMap.get(key))
    }
    new ExampleList(examplesList)
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
