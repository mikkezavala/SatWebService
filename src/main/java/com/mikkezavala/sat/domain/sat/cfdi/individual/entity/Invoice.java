package com.mikkezavala.sat.domain.sat.cfdi.individual.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Comprobante")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Invoice {

  @JsonProperty
  @XmlAttribute(name = "Serie")
  private String serial;

  @JsonProperty
  @XmlAttribute(name = "Folio")
  private String folio;

  @JsonProperty
  @XmlElement(name = "cfdi:Emisor")
  private Issuer issuer;

  @JsonProperty
  @XmlElement(name = "cfdi:Receptor")
  private Receptor receptor;

  @JsonProperty
  @JsonInclude(NON_NULL)
  @XmlElement(name = "cfdi:Complemento")
  private Complemento complement;

  @JsonProperty
  @JsonInclude(NON_NULL)
  @XmlElement(name = "cfdi:Conceptos")
  private Conceptos concepts;

}
