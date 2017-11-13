package smUtils;

import static smUtils.helper.ConsoleService.showAnswer;
import static smUtils.helper.ConsoleService.showError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;
import smUtils.helper.StringToPathConverter;

/**
 * @author bespalko
 * @since 13.11.2017
 */
@ShellComponent
@PropertySources({ @PropertySource(value = "classpath:setup-commands.properties", ignoreResourceNotFound = true) })
public class SetupCommands {
  private List<String> plot;
  private Path projectPath;
  private String cloneFrom;
  private Properties setupProps;
  private Properties defaultSetupProps;
  private Path[] scripts;
  private Path resourcePath;
  private String templatesFrom;
  private Path templatesTo;

  @Autowired
  SetupCommands(Environment env) {
    initProperties(env);
    plot = Arrays.asList("Operations chain for autoInstall:",
                         "\tclone project to path (if need use --clone + clonefrom.url in setup-commands.properties)",
                         "\trun mvn command: clean install -Ptbi", "\tcreateUserDB", "\tconfig schemaDB",
                         "\tconfig resources.properties", "\tdownload templates", "**You must config your IDE manually",
                         "**For more info see: https://devscape02.x5.ru/pages/viewpage.action?pageId=83955371");
  }

  private void initProperties(Environment env) {
    GenericConversionService conversionService = (GenericConversionService) DefaultConversionService
      .getSharedInstance();
    conversionService.addConverter(new StringToPathConverter());
    cloneFrom = env.getProperty("cloneFrom.uri", "https://gitlab.x5.ru/GKC/sm.git");
    projectPath = env.getProperty("projectPath", Path.class, Paths.get(URI.create("file:///C:/bespalko/smUtilsTest")));

    String driver = env.getProperty("resource.ConnectionPool.driver", "oracle.jdbc.driver.OracleDriver");
    String url = env.getProperty("resource.ConnectionPool.url", "jdbc:oracle:thin:@x5-db-dev1.dfu.i-teco.ru:1521:orcl");
    String user = env.getProperty("resource.ConnectionPool.user", "SM");
    String password = env.getProperty("resource.ConnectionPool.pass", "SM");
    setupProps = new Properties();
    setupProps.setProperty("resource.ConnectionPool.driver", driver);
    setupProps.setProperty("resource.ConnectionPool.url", url);
    setupProps.setProperty("resource.ConnectionPool.user", user);
    setupProps.setProperty("resource.ConnectionPool.pass", password);

    String scriptPath = "/mod-sm-server/src/main/resources/db/oracle";
    scripts = new Path[] { Paths.get(projectPath.toString(), scriptPath, "create.sql"),
                           Paths.get(projectPath.toString(), scriptPath, "initdata.sql") };
    resourcePath = Paths.get(projectPath.toString(), "mod-sm-server/config", "resources.properties");
    templatesFrom = env.getProperty("templatesFrom.uri",
                                    "https:///devscape02.x5.ru/download/attachments/86738260/templates.rar?version=1&modificationDate=1507894681786&api=v2");
    templatesTo = Paths.get(projectPath.toString(), "mod-sm-server/import/templates/");
  }

  @ShellMethod("show instructions plot")
  public List<String> plot() {
    plot.forEach(System.out::println);
    return null;
  }

  @ShellMethod("auto-install sm-project for dev. Path - URI wanted location.")
  public String auto(@ShellOption(defaultValue = "false") boolean clone) {
    if (clone) {
      clone(cloneFrom, projectPath);
    }
    //init(null);
    //createUser(user, password);
    //applySqlScripts(scripts);
    try {
      config(resourcePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    templates(Paths.get(templatesFrom), templatesTo);
    return null;
  }

  /**
   * clone project from URL to path
   */
  void clone(String from, Path to) {
    runProcess(true, null, "git", "clone", from, to.toString());
  }

  @ShellMethod("run \"mvn clean install\" command. Par - additional paramener (can be -Ptbi)")
  public String init(@ShellOption(defaultValue = "") String par) {
    //TODO dont work
    if (!"".equals(par)) {
      runProcess(true, projectPath, "mvn", "clean", "install", par);
    } else {
      runProcess(true, projectPath, "mvn", "clean", "install");
    }
    return null;
  }

  @ShellMethod("create user in oracle-DB")
  String createUser(String username, String password) {
    return null;
  }

  @ShellMethod("apply to oracle-DB scripts: create.sql, initdata.sql (URI paths)")
  String applySqlScripts(@ShellOption(arity = 2, defaultValue = "") Path[] scriptsSql) {
    if (scriptsSql == null || scriptsSql.length < 1) {
      scriptsSql = this.scripts;
    }
    return null;
  }

  @ShellMethod("config resources.properties")
  public String config(@ShellOption(defaultValue = "") Path propsPath) throws IOException {
    if (propsPath == null || StringUtils.isEmpty(propsPath.toString())) {
      propsPath = resourcePath;
    }
    List<String> resourceFileContent = Files.readAllLines(propsPath);
    mergeProps(resourceFileContent, setupProps);
    Files.write(propsPath, resourceFileContent);
    return null;
  }

  private void mergeProps(List<String> resourceFileContent, Properties forMerge) {
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
        String newValue = setupProps.getProperty(key);
        iter.set(key + '=' + newValue);
      }
    }
  }

  @ShellMethod("downloads templates")
  public String templates(Path from, Path to) {
    return null;
  }

  private void runProcess(boolean showConsole, Path homeDir, String... commands) {
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
