package com.mikkezavala.sat.domain.sat.cfdi.individual;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(fluent = true)
public class SatClient {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private String rfc;

  private String passwordPlain;

  private String keystore;

}
