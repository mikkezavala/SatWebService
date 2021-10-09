package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Complemento {

  @JsonInclude(NON_NULL)
  @XmlElement(name = "pago10:Pagos")
  private Payments payments;

  @JsonInclude(NON_NULL)
  @XmlElement(name = "nomina12:Nomina")
  private Payroll payroll;
}
