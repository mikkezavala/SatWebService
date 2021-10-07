package com.mikkezavala.sat.domain.sat.auth;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AutenticaResponse", namespace = "http://DescargaMasivaTerceros.gob.mx")
public class AuthResponse {

  @XmlElement(name = "AutenticaResult", namespace = "http://DescargaMasivaTerceros.gob.mx")
  private String autenticaResult;

  @Override
  public String toString() {
    return "Token: " + autenticaResult;
  }

}
