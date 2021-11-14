package com.mikkezavala.sat.util;

import static com.mikkezavala.sat.util.Constant.KEY_STORE;
import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.TestBase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type Fiel util test.
 */
public class FielUtilTest extends TestBase {

  /**
   * Should generate pfx.
   *
   * @throws FileNotFoundException the file not found exception
   */

  @BeforeEach
  public void setUp() throws IOException {
    File store = Path.of(KEY_STORE).toFile();
    if (store.exists()) {
      FileUtils.forceDelete(store);
    }
    FileUtils.forceMkdir(store);
  }

  @Test
  public void shouldGeneratePfx() throws FileNotFoundException {
    Path key = loadResourceAsFile("PF_CFDI").toPath().resolve(RFC_TEST + ".key");
    Path cert = loadResourceAsFile("PF_CFDI").toPath().resolve(RFC_TEST + ".cer");
    int exitCode = FielUtil.generateP12(RFC_TEST, cert, key, RFC_TEST_PASS);
    assertThat(exitCode).isEqualTo(0);
  }


  /**
   * Should fail open with wrong password.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void shouldFailOpenWithWrongPassword() throws FileNotFoundException {
    Path key = loadResourceAsFile("PF_CFDI").toPath().resolve(RFC_TEST + ".key");
    Path cert = loadResourceAsFile("PF_CFDI").toPath().resolve(RFC_TEST + ".cer");
    int exitCode = FielUtil.generateP12(RFC_TEST, cert, key, "WRONG");
    assertThat(exitCode).isEqualTo(-1);
  }
}
