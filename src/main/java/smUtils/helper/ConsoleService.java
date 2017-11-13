package smUtils.helper;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author bespalko
 * @since 13.11.2017
 */
@Service
public class ConsoleService {

  private static final Logger log = LoggerFactory.getLogger(StringToPathConverter.class);
  private static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_RED = "\u001B[31m";

  private static final PrintStream out = System.out;

  public static PrintStream getOut() {
    return out;
  }

  public static void showAnswer(String msg, Object... args) {
    out.printf("> ");
    out.print(ANSI_YELLOW);
    out.printf(msg, (Object[]) args);
    out.print(ANSI_RESET);
    out.println();
  }

  public static void showError(Throwable ex, String msg, Object... args) {
    out.print(ANSI_RED);
    out.printf(msg, (Object[]) args);
    out.print(ANSI_RESET);
    out.println();
    log.debug(msg, ex);
  }
}
