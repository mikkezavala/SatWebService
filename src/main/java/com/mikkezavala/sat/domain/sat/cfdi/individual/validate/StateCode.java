package com.mikkezavala.sat.domain.sat.cfdi.individual.validate;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The enum State code.
 */
@XmlEnum(Integer.class)
public enum StateCode {
  /**
   * Accepted state code.
   */
  @XmlEnumValue("1") ACCEPTED,
  /**
   * In progress state code.
   */
  @XmlEnumValue("2") IN_PROGRESS,
  /**
   * Ready state code.
   */
  @XmlEnumValue("3") READY,
  /**
   * Error state code.
   */
  @XmlEnumValue("4") ERROR,
  /**
   * Rejected state code.
   */
  @XmlEnumValue("5") REJECTED,
  /**
   * Expired state code.
   */
  @XmlEnumValue("6") EXPIRED;

  private static final Map<String, StateCode> ITEMS = new HashMap<>();

  static {
    for (StateCode code : values()) {
      ITEMS.put(code.name(), code);
    }
  }

  /**
   * Gets code.
   *
   * @param value the value
   * @return the code
   */
  public static StateCode getCode(String value) {
    return ITEMS.get(value);
  }

  /**
   * Equals boolean.
   *
   * @param value the value to test
   * @return the boolean
   */
  public static boolean equals(String value, StateCode code) {
    return ITEMS.get(value).equals(code);
  }
}
