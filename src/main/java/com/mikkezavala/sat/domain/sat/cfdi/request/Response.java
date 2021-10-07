package com.mikkezavala.sat.domain.sat.cfdi.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SolicitaDescargaResponse", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class Response {

  @XmlElement(name = "SolicitaDescargaResult", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private Result result;
}
