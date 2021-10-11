package com.mikkezavala.sat.domain.sat.cfdi.individual;

import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SatToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private String rfc;

  private String token;

  private ZonedDateTime created;

  private ZonedDateTime expiration;

}
