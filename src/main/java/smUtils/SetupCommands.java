package smUtils;

import static smUtils.helper.ConsoleService.showAnswer;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
import smUtils.helper.ProcessUtil;
import smUtils.helper.PropUtil;
import smUtils.helper.StringToPathConverter;

/**
 * @author bespalko
 * @since 13.11.2017
 */
@ShellComponent
@PropertySources({ @PropertySource(value = "classpath:setup-commands.properties", ignoreResourceNotFound = true) })
public class SetupCommands {
  private Environment env;
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
    this.env = env;
    initProperties(env);
    plot = Lists.newArrayList("Operations chain for autoInstall:",
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
  public void plot() {
    plot.forEach(line -> showAnswer(line));
  }

  @ShellMethod("reinit properties")
  public void reinit() {
    initProperties(env);
    showAnswer("reinit complete");
  }

  @ShellMethod("auto-install sm-project for dev. Path - URI wanted location.")
  public void auto(@ShellOption(defaultValue = "false") boolean clone) {
    if (clone) {
      clone(cloneFrom, projectPath);
    }
    //install(null);
    //createUser(user, password);
    //applySqlScripts(scripts);
    try {
      config(resourcePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    templates(Paths.get(templatesFrom), templatesTo);
  }

  /**
   * clone project from URL to path
   */
  @ShellMethod("clone sm-project")
  void clone(String from, Path to) {
    ArrayList<String> commands = Lists.newArrayList("git", "clone", from, to.toString());
    ProcessUtil.exec(true, null, commands);
    showAnswer("%s is complete\ncloned into: %s", commands, to);
  }

  @ShellMethod("run \"mvn clean install\" command. Par - additional paramener (can be -Ptbi)")
  public void install(@ShellOption(defaultValue = "") String par) {
    List<String> commands = Lists.newArrayList("mvn", "clean", "install");
    if(!StringUtils.isEmpty(par)) {
      commands.add(par);
    }
    //TODO dont work
    //runProcess(true, projectPath, commands);
    showAnswer("%s is complete", commands);
  }

  @ShellMethod("create user in oracle-DB")
  String createUser(String username, String password) {
    return null;
  }

  @ShellMethod("apply to oracle-DB scripts: create.sql, initdata.sql (URI paths)")
  void applySqlScripts(@ShellOption(arity = 2, defaultValue = "") Path[] scriptsSql) {
    if (scriptsSql == null || scriptsSql.length < 1) {
      scriptsSql = this.scripts;
    }

    for (Path script : scriptsSql) {
      showAnswer("%s applied", script);
    }
  }

  @ShellMethod("config resources.properties")
  public void config(@ShellOption(defaultValue = "") Path propsPath) throws IOException {
    if (propsPath == null || StringUtils.isEmpty(propsPath.toString())) {
      propsPath = resourcePath;
    }
    List<String> resourceFileContent = Files.readAllLines(propsPath);
    PropUtil.mergeProps(resourceFileContent, setupProps);
    Files.write(propsPath, resourceFileContent);
    showAnswer("%s configured", propsPath);
  }

  @ShellMethod("downloads templates")
  public void templates(Path from, Path to) {
    //download from
    //unzip
    //save to
    showAnswer("templates downloaded & unzipped\nto: ",  to);
  }
}
