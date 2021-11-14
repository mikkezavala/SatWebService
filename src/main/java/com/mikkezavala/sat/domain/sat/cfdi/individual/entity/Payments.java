package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Payments {

  @JsonProperty
  @XmlElement(name = "pago10:Pago")
  private Payment payment;

}
