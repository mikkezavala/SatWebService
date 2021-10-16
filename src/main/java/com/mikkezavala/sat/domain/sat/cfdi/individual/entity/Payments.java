package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Payments {

  @XmlElement(name = "pago10:Pago")
  private Payment payment;

}
