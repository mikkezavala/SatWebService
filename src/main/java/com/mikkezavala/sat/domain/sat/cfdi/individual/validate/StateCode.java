package com.mikkezavala.sat.domain.sat.cfdi.individual.validate;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum StateCode {
  @XmlEnumValue("1") ACCEPTED,
  @XmlEnumValue("2") IN_PROGRESS,
  @XmlEnumValue("3") READY,
  @XmlEnumValue("4") ERROR,
  @XmlEnumValue("5") REJECTED,
  @XmlEnumValue("6") EXPIRED;
}
