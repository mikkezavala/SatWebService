package com.mikkezavala.sat.domain.sat.cfdi.individual.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Result {

  @XmlAttribute(name = "IdSolicitud")
  private String requestId;

  @XmlAttribute(name = "CodEstatus")
  private String status;

  @XmlAttribute(name = "Mensaje")
  private String message;
}
