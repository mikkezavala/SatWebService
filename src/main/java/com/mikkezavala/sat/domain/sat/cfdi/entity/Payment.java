package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;


@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Payment {

  @XmlAttribute(name = "FechaPago")
  private String date;

  @XmlAttribute(name = "FormaDePagoP")
  private String paymentForm;

  @XmlAttribute(name = "MonedaP")
  private String currency;

  @XmlAttribute(name = "Monto")
  private double amount;

}
