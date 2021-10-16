package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Issuer {

  @XmlAttribute(name = "Rfc")
  private String rfc;

  @XmlAttribute(name = "Nombre")
  private String name;

  @XmlAttribute(name = "RegimenFiscal")
  private String fiscalRegime;

}
