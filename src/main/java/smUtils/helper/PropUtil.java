package smUtils.helper;

import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

/**
 * @author bespalko
 * @since 13.11.2017
 */
public class PropUtil {
  public static void mergeProps(List<String> resourceFileContent, Properties forMerge) {
    ListIterator<String> iter = resourceFileContent.listIterator();
    while (iter.hasNext()) {
      String line = iter.next().trim();
      if (line.startsWith("#")) {
        continue;
      }
      int eqIndex = line.indexOf('=');
      if (eqIndex == -1) {
        continue;
      }
      String key = line.substring(0, eqIndex - 1).trim();
      if (forMerge.contains(key)) {
        String newValue = forMerge.getProperty(key);
        iter.set(key + '=' + newValue);
      }
    }
  }
}
