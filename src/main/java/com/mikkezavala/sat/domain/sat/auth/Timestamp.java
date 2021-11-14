package com.mikkezavala.sat.domain.sat.auth;

import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;

import com.mikkezavala.sat.adapter.ZonedDateTimeAdapter;
import java.time.ZonedDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Timestamp", namespace = WSS_UTILITY_NS)
public class Timestamp {

  @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
  @XmlElement(name = "Created", namespace = WSS_UTILITY_NS)
  private ZonedDateTime created;

  @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
  @XmlElement(name = "Expires", namespace = WSS_UTILITY_NS)
  private ZonedDateTime expires;
}
