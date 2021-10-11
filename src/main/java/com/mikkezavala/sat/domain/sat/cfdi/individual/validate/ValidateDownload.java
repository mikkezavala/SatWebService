package com.mikkezavala.sat.domain.sat.cfdi.individual.validate;

import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VerificaSolicitudDescarga", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
public class ValidateDownload {

  @XmlElement(name = "solicitud", namespace = "http://DescargaMasivaTerceros.sat.gob.mx")
  private Request request;
}
