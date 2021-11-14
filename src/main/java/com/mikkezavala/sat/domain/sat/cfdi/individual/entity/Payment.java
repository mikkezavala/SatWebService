package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Payment {

  @JsonProperty
  @XmlAttribute(name = "FechaPago")
  private String date;

  @JsonProperty
  @XmlAttribute(name = "FormaDePagoP")
  private String paymentForm;

  @JsonProperty
  @XmlAttribute(name = "MonedaP")
  private String currency;

  @JsonProperty
  @XmlAttribute(name = "Monto")
  private double amount;

}
