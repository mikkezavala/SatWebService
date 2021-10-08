package com.mikkezavala.sat.domain.sat.cfdi.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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

  @JsonInclude(NON_NULL)
  @XmlElement(name = "cfdi:Complemento")
  private Complemento complement;

  @JsonInclude(NON_NULL)
  @XmlElement(name = "cfdi:Conceptos")
  private Conceptos concepts;

}
