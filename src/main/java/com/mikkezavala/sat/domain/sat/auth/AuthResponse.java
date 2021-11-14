package com.mikkezavala.sat.domain.sat.auth;

import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_NS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AutenticaResponse", namespace = SAT_DESCARGA_NS)
public class AuthResponse {

  @XmlElement(name = "AutenticaResult", namespace = SAT_DESCARGA_NS)
  private String token;

  private Timestamp timestamp;
}
