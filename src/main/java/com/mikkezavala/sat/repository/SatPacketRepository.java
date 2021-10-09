package com.mikkezavala.sat.repository;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import java.time.ZonedDateTime;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SatPacketRepository extends CrudRepository<SatPacket, Integer> {

  SatPacket findSatPacketByRfcAndDateEndAndDateStart(
      String rfc, ZonedDateTime dateEnd, ZonedDateTime dateStart
  );
}

