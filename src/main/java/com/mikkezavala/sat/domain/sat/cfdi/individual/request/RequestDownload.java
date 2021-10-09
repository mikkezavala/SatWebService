package com.mikkezavala.sat.domain.sat.cfdi.individual.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SolicitaDescarga", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class RequestDownload {

  @XmlElement(name = "solicitud", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private Request request;

}
