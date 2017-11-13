package smUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import smUtils.helper.ConsoleService;
import smUtils.helper.StringToPathConverter;

import static smUtils.helper.ConsoleService.showAnswer;

/**
 * @author bespalko
 * @since 13.11.2017
 */
@ShellComponent
@PropertySources({ @PropertySource(value = "classpath:setup-commands.properties", ignoreResourceNotFound = true) })
public class SetupCommands {
  private List<String> plot;
  private String CLONE_FROM;
  private Path PROJECT_PATH;

  @Autowired
  SetupCommands(Environment env) {
    initProperties(env);
    plot = Arrays.asList("Operations chain for autoInstall:",
                         "\tclone project to path (if need use --clone + clonefrom.url in setup-commands.properties)",
                         "\trun mvn command: clean install -Ptbi",
                         "\tcreateUserDB", "config schemaDB",
                         "\tconfig resources.properties",
                         "\tdownload templates",
                         "**You must config your IDE manually",
                         "**For more info see: https://devscape02.x5.ru/pages/viewpage.action?pageId=83955371");
  }

  private void initProperties(Environment env) {
    GenericConversionService conversionService = (GenericConversionService) DefaultConversionService.getSharedInstance();
    conversionService.addConverter(new StringToPathConverter());
    CLONE_FROM = env.getProperty("cloneFrom.url");
    PROJECT_PATH = env.getProperty("projectPath", Path.class);
  }

  @ShellMethod("show instructions plot")
  public List<String> plot() {
    return plot;
  }

  @ShellMethod("auto-install sm-project for dev. Path - URI wanted location.")
  public String auto(@ShellOption(defaultValue = "false") boolean clone) {
    if(clone) {
      clone(CLONE_FROM, PROJECT_PATH);
    }
    return null;
  }

  @ShellMethod("clone project from URL to path (both in URI)")
  public void clone(String from, Path to) {
    ProcessBuilder pb = new ProcessBuilder("git", "clone", from);
    Map<String, String> env = pb.environment();
    pb.directory(to.toFile());
    try {
      pb.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
/*    try {
      //String command = "cmd /c " + cd ${boHome}${boMid} && mvn generate-sources";
      //List<String> command
     // runProcess(command, true);
      showAnswer("cloned from %s to %s", from, to);
    } catch (IOException e) {
      e.printStackTrace();
    }*/
  }

  @ShellMethod("run \"mvn clean install\" command. Par - additional paramener (can be -Ptbi)")
  public String init(@ShellOption(defaultValue = "") String par) {
    return null;
  }

  @ShellMethod("create user in oracle-DB")
  public String createUser(String username, String password) {
    return null;
  }

  @ShellMethod("apply to oracle-DB scripts: create.sql, initdata.sql")
  public String applySqlScripts(@ShellOption(arity = 2) Path[] paths) {
    return null;
  }

  @ShellMethod("config resources.properties")
  public String config(@ShellOption(defaultValue = "") Path path) {
    return null;
  }

  @ShellMethod("downloads templates")
  public String templates(Path from, Path to) {
    return null;
  }

  private void runProcess(String command, boolean showConsole) throws IOException {
    Process pr = Runtime.getRuntime().exec(command);
    if(showConsole) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        showAnswer(line);
      }
    }
  }
}
