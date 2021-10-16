package com.mikkezavala.sat.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The type Xml util test.
 */
public class XMLUtilTest {

  /**
   * Should fail and return empty string.
   */
  @Test
  public void shouldFailAndReturnEmptyString() {
    String request = XmlUtil.serialize("❤️", true);
    assertThat(request).isEmpty();
  }

}
