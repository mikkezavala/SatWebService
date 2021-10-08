package com.mikkezavala.sat.domain.sat.cfdi.entity;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Conceptos {


  @XmlElement(name = "cfdi:Concepto")
  private List<Concepto> concept;

}
