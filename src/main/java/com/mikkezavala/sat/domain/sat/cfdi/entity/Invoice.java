package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Comprobante")
public class Invoice {

  @XmlAttribute(name = "Serie")
  private String serial;

  @XmlAttribute(name = "Folio")
  private String folio;

  @XmlElement(name = "cfdi:Emisor")
  private Issuer issuer;

  @XmlElement(name = "cfdi:Receptor")
  private Receptor receptor;

  @XmlElement(name = "cfdi:Complemento")
  private Complemento compliment;

}
