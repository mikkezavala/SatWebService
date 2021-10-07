package com.mikkezavala.sat.domain;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SatClient {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private String rfc;

  private String passwordPlain;

  private String keystore;

}
