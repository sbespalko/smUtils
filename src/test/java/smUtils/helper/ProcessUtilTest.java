package smUtils.helper;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

/**
 * @author bespalko
 * @since 14.11.2017
 */
public class ProcessUtilTest {
  List<String> commands;

  @Before
  public void setUp() throws Exception {
    commands = Lists.newArrayList("cmd", "dir");
  }

  @Test
  public void basicTest() throws Exception {
    ProcessUtil.create(commands).showConsole().run();
  }

}