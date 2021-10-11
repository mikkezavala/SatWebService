package com.mikkezavala.sat.repository;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SatTokenRepository extends CrudRepository<SatToken, Integer> {

  SatToken findFirstByRfc(String rfc);

  @Transactional
  void deleteByRfc(String integer);
}

