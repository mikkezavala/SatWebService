package com.mikkezavala.sat.domain.sat.auth;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Timestamp {

  private ZonedDateTime created;

  private ZonedDateTime expires;
}
