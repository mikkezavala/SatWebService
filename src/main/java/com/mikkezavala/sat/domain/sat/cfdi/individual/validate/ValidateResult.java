package com.mikkezavala.sat.domain.sat.cfdi.individual.validate;

import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidateResult {

  @XmlAttribute(name = "CodEstatus")
  private String status;

  @XmlAttribute(name = "EstadoSolicitud")
  private StateCode state;

  @XmlAttribute(name = "Mensaje")
  private String message;

  @XmlAttribute(name = "NumeroCFDIs")
  private int cfdiCount;

  @XmlElement(name = "IdsPaquetes", namespace = SAT_DESCARGA_MASIVA_NS)
  private List<String> IdsPaquetes;

}
