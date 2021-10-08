package com.mikkezavala.sat.domain.sat.cfdi.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Concepto {

  @XmlAttribute(name = "Cantidad")
  private double amount;

  @XmlAttribute(name = "ClaveProdServ")
  private String serviceCode;

  @XmlAttribute(name = "ClaveUnidad")
  private String unitCode;

  @XmlAttribute(name = "Descripcion")
  private String description;

  @XmlAttribute(name = "Descuento")
  private double discount;

  @XmlAttribute(name = "Importe")
  private double importAmount;

  @XmlAttribute(name = "NoIdentificacion")
  private String idNumber;

  @XmlAttribute(name = "Unidad")
  private String unit;

  @XmlAttribute(name = "ValorUnitario")
  private double unitValue;
}
