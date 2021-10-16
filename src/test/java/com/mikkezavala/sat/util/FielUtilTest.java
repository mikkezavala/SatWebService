package com.mikkezavala.sat.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.TestBase;
import java.io.FileNotFoundException;
import java.nio.file.Path;
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
  @Test
  public void shouldGeneratePfx() throws FileNotFoundException {
    Path key = loadResource("PF_CFDI").toPath().resolve(RFC_TEST + ".key");
    Path cert = loadResource("PF_CFDI").toPath().resolve(RFC_TEST + ".cer");
    int exitCode = FielUtil.generatePFX(RFC_TEST, cert, key, RFC_TEST_PASS);
    assertThat(exitCode).isEqualTo(0);
  }


  /**
   * Should fail open with wrong password.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void shouldFailOpenWithWrongPassword() throws FileNotFoundException {
    Path key = loadResource("PF_CFDI").toPath().resolve(RFC_TEST + ".key");
    Path cert = loadResource("PF_CFDI").toPath().resolve(RFC_TEST + ".cer");
    int exitCode = FielUtil.generatePFX(RFC_TEST, cert, key, "WRONG");
    assertThat(exitCode).isEqualTo(-1);
  }
}
