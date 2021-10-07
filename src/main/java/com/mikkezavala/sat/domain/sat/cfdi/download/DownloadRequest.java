package com.mikkezavala.sat.domain.sat.cfdi.download;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "peticionDescarga")
public class DownloadRequest {

  @XmlAttribute(name = "IdPaquete")
  private String IdPaquete;

  @XmlAttribute(name = "RfcSolicitante")
  private String RfcSolicitante;

}
