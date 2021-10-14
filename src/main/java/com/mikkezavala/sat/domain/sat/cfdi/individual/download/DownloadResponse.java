package com.mikkezavala.sat.domain.sat.cfdi.individual.download;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RespuestaDescargaMasivaTercerosSalida", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class DownloadResponse {

  @XmlElement(name = "Paquete", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private String paquete;

}
