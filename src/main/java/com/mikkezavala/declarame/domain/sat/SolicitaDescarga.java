package com.mikkezavala.declarame.domain.sat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SolicitaDescarga", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class SolicitaDescarga {

  @XmlElement(name = "solicitud")
  private Solicitud solicitud;

}
