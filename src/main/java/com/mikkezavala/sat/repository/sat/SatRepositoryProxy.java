package com.mikkezavala.sat.repository.sat;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.repository.SatClientRepository;
import com.mikkezavala.sat.repository.SatPacketRepository;
import com.mikkezavala.sat.repository.SatTokenRepository;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class SatRepositoryProxy implements SatRepository {

  private final SatTokenRepository tokenRepo;

  private final SatClientRepository clientRepo;

  private final SatPacketRepository packetRepo;

  private static final String LOGGER_PREFIX = "[SAT PROXY REPOSITORY]: {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(SatRepositoryProxy.class);

  public SatRepositoryProxy(
      SatTokenRepository tokenRepo,
      SatClientRepository clientRepo,
      SatPacketRepository packetRepo
  ) {
    this.tokenRepo = tokenRepo;
    this.clientRepo = clientRepo;
    this.packetRepo = packetRepo;
  }

  @Override
  public SatClient satClientByRfc(String rfc) {
    LOGGER.info(LOGGER_PREFIX, "satClientByRfc");
    return clientRepo.findSatClientByRfc(rfc);
  }

  @Override
  public SatToken saveToken(SatToken token) {
    LOGGER.info(LOGGER_PREFIX, "saveToken");
    return tokenRepo.save(token);
  }

  @Override
  public SatToken tokenByRfc(String rfc) {
    LOGGER.info(LOGGER_PREFIX, "tokenByRfc");
    return tokenRepo.findFirstByRfc(rfc);
  }

  @Override
  public void tokenDeleteByRfc(String rfc) {
    LOGGER.info(LOGGER_PREFIX, "tokenDeleteByRfc");
    tokenRepo.deleteByRfc(rfc);
  }

  @Override
  public SatPacket savePacket(SatPacket packet) {
    LOGGER.info(LOGGER_PREFIX, "savePacket");
    return packetRepo.save(packet);
  }

  @Override
  public SatPacket packetByRfcAndDateEndAndDateStart(
      String rfc, ZonedDateTime dateEnd, ZonedDateTime dateStart
  ) {
    LOGGER.info(LOGGER_PREFIX, "packetByRfcAndDateEndAndDateStart");
    return packetRepo.findSatPacketByRfcAndDateEndAndDateStart(rfc, dateEnd, dateStart);
  }

}
