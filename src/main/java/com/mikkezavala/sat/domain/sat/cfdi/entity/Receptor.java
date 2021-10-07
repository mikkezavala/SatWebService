package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Receptor {

  @XmlAttribute(name = "Rfc")
  private String rfc;

  @XmlAttribute(name = "Nombre")
  private String name;

  @XmlAttribute(name = "UsoCFDI")
  private String cfdiUse;

}
