package com.mikkezavala.sat.repository.sat;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import java.time.ZonedDateTime;

/**
 * The interface Sat repository.
 */
public interface SatRepository {

  /**
   * Sat client by rfc sat client.
   *
   * @param rfc the rfc
   * @return the sat client
   */
  SatClient satClientByRfc(String rfc);

  /**
   * Save token sat token.
   *
   * @param token the token
   * @return the sat token
   */
  SatToken saveToken(SatToken token);

  /**
   * Token by rfc sat token.
   *
   * @param rfc the rfc
   * @return the sat token
   */
  SatToken tokenByRfc(String rfc);

  /**
   * Token delete by rfc.
   *
   * @param rfc the rfc
   */
  void tokenDeleteByRfc(String rfc);

  /**
   * Save packet sat packet.
   *
   * @param packet the packet
   * @return the sat packet
   */
  SatPacket savePacket(SatPacket packet);

  /**
   * Packet by rfc and date end and date start sat packet.
   *
   * @param rfc       the rfc
   * @param dateEnd   the date end
   * @param dateStart the date start
   * @return the sat packet
   */
  SatPacket packetByRfcAndDateEndAndDateStart(
      String rfc, ZonedDateTime dateEnd, ZonedDateTime dateStart
  );

}
