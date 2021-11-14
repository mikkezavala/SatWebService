package com.mikkezavala.sat.domain.sat.auth;

import static com.mikkezavala.sat.util.Constant.WSS_SEC_EXT_NS;
import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Security", namespace = WSS_SEC_EXT_NS)
public class Security {

  @XmlElement(name = "Timestamp", namespace = WSS_UTILITY_NS)
  private Timestamp timestamp;
}
