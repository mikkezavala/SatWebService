package com.mikkezavala.sat.domain.sat.cfdi.validate;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidateResult {

  @XmlAttribute(name = "CodEstatus")
  private String status;


  @XmlAttribute(name = "EstadoSolicitud")
  private int state;

  @XmlAttribute(name = "Mensaje")
  private String message;

  @XmlAttribute(name = "NumeroCFDIs")
  private int cfdiCount;

  @XmlElement(name = "IdsPaquetes", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private List<String> IdsPaquetes;

}
