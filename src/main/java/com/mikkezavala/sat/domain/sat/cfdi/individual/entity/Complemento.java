package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Complemento {

  @JsonProperty
  @JsonInclude(NON_NULL)
  @XmlElement(name = "pago10:Pagos")
  private Payments payments;

  @JsonProperty
  @JsonInclude(NON_NULL)
  @XmlElement(name = "nomina12:Nomina")
  private Payroll payroll;
}
