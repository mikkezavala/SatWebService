package com.mikkezavala.sat.domain.client.registered;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class RequestCFDI {

  String rfc;

  ZonedDateTime dateEnd;

  ZonedDateTime dateStart;

}
