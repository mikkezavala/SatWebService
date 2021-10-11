package com.mikkezavala.sat.repository;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import org.springframework.data.repository.CrudRepository;

public interface SatClientRepository extends CrudRepository<SatClient, Integer> {

  SatClient findSatClientByRfc(String rfc);
}

