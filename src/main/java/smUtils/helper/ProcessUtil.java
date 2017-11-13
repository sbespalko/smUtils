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

  public static void exec(boolean showConsole, Path homeDir, List<String> commands) {
    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    if (homeDir != null) {
      processBuilder.directory(homeDir.toFile());
    }
    try {
      Process process = processBuilder.start();
      process.waitFor();
      if (showConsole) {
        //TODO dont work
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          showAnswer(line);
        }
      }
    } catch (IOException e) {
      showError(e, "IO promlem. See log");
    } catch (InterruptedException e) {
      showError(e, "Process Interrupted promlem. See log");
    }
  }
}
