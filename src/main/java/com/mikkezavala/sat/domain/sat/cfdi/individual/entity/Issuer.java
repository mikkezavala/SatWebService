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
public class Issuer {

  @JsonProperty
  @XmlAttribute(name = "Rfc")
  private String rfc;

  @JsonProperty
  @XmlAttribute(name = "Nombre")
  private String name;

  @JsonProperty
  @XmlAttribute(name = "RegimenFiscal")
  private String fiscalRegime;

}
