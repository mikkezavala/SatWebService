package com.mikkezavala.sat.util;

import static com.mikkezavala.sat.util.ResourceUtil.ZIP_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type Resource util test.
 */
public class ResourceUtilTest extends TestBase {

  /**
   * Sets up.
   *
   * @throws IOException the io exception
   */
  @BeforeEach
  public void setUp() throws IOException {
    File zipDir = Path.of(ZIP_PREFIX).toFile();
    if (zipDir.exists()) {
      FileUtils.forceDelete(zipDir);
    }
  }

  /**
   * Should zip.
   */
  @Test
  public void shouldZip() {
    ResourceUtil.saveZip("FakeContent", UUID.randomUUID().toString());
  }

  /**
   * Gets from zip.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void getFromZip() throws FileNotFoundException {
    List<Invoice> invoices = ResourceUtil.getFromZip(
        loadResource("demo.zip").toString(), Invoice.class
    );

    assertThat(invoices).hasSize(1);
  }

  /**
   * Should fail read zip.
   */
  @Test
  public void shouldFailReadZip() {
    List<Object> list = ResourceUtil.getFromZip("non.zip", Object.class);

    assertThat(list).hasSize(0);

  }
}
