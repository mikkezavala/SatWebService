package com.mikkezavala.sat.repository;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SatClientRepository extends CrudRepository<SatClient, Integer> {

  SatClient findSatClientByRfc(String rfc);

  @Transactional
  void deleteAllByRfc(String rfc);
}

