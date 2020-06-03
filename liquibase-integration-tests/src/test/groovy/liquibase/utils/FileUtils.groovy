package liquibase.utils

import groovy.io.FileType
import org.spockframework.util.IoUtil

import java.nio.file.Paths
import java.util.function.Predicate

class FileUtils {


  static URL getResourceFileAsURL(String filename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader()
    return loader.getResource(filename)
  }

  static String getResourceFileContent(String filename) {
    URL url = getResourceFileAsURL(filename)
    if (url != null) {
      return IoUtil.getText(Paths.get(url.toURI()).toFile())
    }
    return null
  }


  static void writeToFile(String filename, String content) {
    File file = new File(filename)
    try {
      file.write(content)
    } catch (IOException e) {
      e.printStackTrace()
    }
  }

  /**
   * Returns list of file names.
   *
   * @param dirPath
   * @param remove_extension if true removes the extension from the list of file names returned
   * @param filterPredicate if passed a {@link Predicate} which takes a file and returns a boolean,
   * only those files that return true to this predicate function will be returned.
   * This can be use used to filter files based on different properties. A null value (default)
   * will not filter the files.
   * @return
   */
  static List<String> getFileList(String dirPath, boolean remove_extension = false, Predicate<File> filterPredicate = null) {
    List<String> list = []
    URL url = getResourceFileAsURL(dirPath)
    File dir = Paths.get(url.toURI()).toFile()

    dir.exists() && dir.eachFile(FileType.FILES) { file ->
      if (filterPredicate == null || filterPredicate.test(file)) {
        String fileName = file.getName()
        if (remove_extension) {
          fileName = fileName.take(fileName.lastIndexOf('.'))
        }
        list << fileName
      }

    }
    return list
  }


}
