package com.mikkezavala.sat.repository;

import com.mikkezavala.sat.domain.SatClient;
import org.springframework.data.repository.CrudRepository;

public interface SatClientRepository extends CrudRepository<SatClient, Integer> {

  SatClient findSatClientByRfc(String rfc);
}

