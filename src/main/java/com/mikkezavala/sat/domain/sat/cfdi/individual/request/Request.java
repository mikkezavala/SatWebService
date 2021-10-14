package com.mikkezavala.sat.domain.sat.cfdi.individual.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solicitud")
public class Request {

  @XmlAttribute(name = "IdSolicitud")
  private String id;

  @XmlAttribute(name = "FechaFinal")
  private String dateEnd;

  @XmlAttribute(name = "FechaInicial")
  private String dateStart;

  @XmlAttribute(name = "RfcEmisor")
  private String rfcIssuer;

  @XmlAttribute(name = "RfcReceptor")
  private String rfcReceptor;

  @XmlAttribute(name = "RfcSolicitante")
  private String rfcRequest;

  @XmlAttribute(name = "TipoSolicitud")
  private String requestType;

}