package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Complemento {

  @XmlElement(name = "pago10:Pagos")
  private Payments payments;

  @XmlElement(name = "nomina12:Nomina")
  private Nomina payroll;
}
