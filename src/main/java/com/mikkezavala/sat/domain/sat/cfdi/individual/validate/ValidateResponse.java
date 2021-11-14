package com.mikkezavala.sat.domain.sat.cfdi.individual.validate;

import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VerificaSolicitudDescargaResponse", namespace = SAT_DESCARGA_MASIVA_NS)
public class ValidateResponse {

  @XmlElement(name = "VerificaSolicitudDescargaResult", namespace = SAT_DESCARGA_MASIVA_NS)
  private ValidateResult result;
}
