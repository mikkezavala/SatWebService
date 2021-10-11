package com.mikkezavala.sat.domain.client.registered;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class RequestCfdi {

  String rfc;

  ZonedDateTime dateEnd;

  ZonedDateTime dateStart;

}
