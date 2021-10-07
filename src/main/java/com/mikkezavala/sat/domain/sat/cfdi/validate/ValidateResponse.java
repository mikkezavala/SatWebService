package com.mikkezavala.sat.domain.sat.cfdi.validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VerificaSolicitudDescargaResponse", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class ValidateResponse {

  @XmlElement(name = "VerificaSolicitudDescargaResult", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private ValidateResult result;
}
