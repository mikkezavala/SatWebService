package com.mikkezavala.sat.domain.sat.cfdi.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solicitud")
public class Request {

  @XmlAttribute(name = "IdSolicitud")
  private String id;

  @XmlAttribute(name = "FechaFinal")
  private String fechaFinal;

  @XmlAttribute(name = "FechaInicial")
  private String fechaInicial;

  @XmlAttribute(name = "RfcEmisor")
  private String rfcEmisor;

  @XmlAttribute(name = "RfcReceptor")
  private String rfcReceptor;

  @XmlAttribute(name = "RfcSolicitante")
  private String rfcSolicitante;

  @XmlAttribute(name = "TipoSolicitud")
  private String tipoSolicitud;

}