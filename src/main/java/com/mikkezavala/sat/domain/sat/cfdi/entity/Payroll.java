package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Payroll {

  @XmlAttribute(name = "TotalDeducciones")
  private double totalDeductions;

  @XmlAttribute(name = "TotalPercepciones")
  private double totalPerception;

  @XmlAttribute(name = "NumDiasPagados")
  private double daysPaid;

  @XmlAttribute(name = "FechaInicialPago")
  private String dateStart;

  @XmlAttribute(name = "FechaPago")
  private String paymentDate;

}
