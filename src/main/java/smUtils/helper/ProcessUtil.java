package smUtils.helper;

import static smUtils.helper.ConsoleService.showAnswer;
import static smUtils.helper.ConsoleService.showError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

/**
 * @author bespalko
 * @since 13.11.2017
 */
public class ProcessUtil {
  private ProcessBuilder processBuilder;
  private boolean showConsole;
  private Path logfile;

  private ProcessUtil(List<String> commands) {
    processBuilder = new ProcessBuilder(commands);
  }

  public static ProcessUtil create(List<String> commands) {
    return new ProcessUtil(commands);
  }

  public void run() throws IOException, InterruptedException {
    Process process = processBuilder.start();
    if (showConsole) {
      //TODO dont work
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        showAnswer(line);
      }
    }
    process.waitFor();
  }

  public ProcessUtil homeDir(Path homeDir) {
    processBuilder.directory(homeDir.toFile());
    return this;
  }

  public ProcessUtil logfile(Path logfile) {
    this.logfile = logfile;
    return this;
  }

  public ProcessUtil showConsole() {
    this.showConsole = true;
    return this;
  }
}
