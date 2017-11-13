package smUtils.helper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author bespalko
 * @since 13.11.2017
 */
@Component
final public class StringToPathConverter implements Converter<String, Path> {


  private static final String INCORRECT_URI_FORMAT = "Incorrect URI format: ";

  @Override
  public Path convert(String source) {
    if(StringUtils.isEmpty(source)) {
      return null;
    }
    Path result = null;
    try {
      result = Paths.get(URI.create(source));
    } catch (IllegalArgumentException ex) {
      ConsoleService.showError(ex, INCORRECT_URI_FORMAT + source);

    }
    return result;
  }
}
