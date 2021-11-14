package com.mikkezavala.sat.repository.sat;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.repository.SatClientRepository;
import com.mikkezavala.sat.repository.SatPacketRepository;
import com.mikkezavala.sat.repository.SatTokenRepository;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Sat repository proxy test.
 */
@ExtendWith(SpringExtension.class)
class SatRepositoryProxyTest extends TestBase {

  @Mock
  private SatTokenRepository tokenRepo;

  @Mock
  private SatClientRepository clientRepo;

  @Mock
  private SatPacketRepository packetRepo;

  @InjectMocks
  private SatRepositoryProxy repositoryProxy;

  private static final String TOKEN_MOCK = "ejToken123";

  /**
   * Should retrieve sat client by rfc.
   */
  @Test
  public void shouldRetrieveSatClientByRfc() {
    repositoryProxy.satClientByRfc(RFC_TEST);
    verify(clientRepo, times(1)).findSatClientByRfc(any());
  }

  /**
   * Should save token.
   */
  @Test
  public void shouldSaveToken() {
    SatToken token = SatToken.builder().id(1).rfc(RFC_TEST).token(TOKEN_MOCK).build();
    repositoryProxy.saveToken(token);

    verify(tokenRepo, times(1)).save(any());
  }

  /**
   * Should retrieve token by rfc.
   */
  @Test
  public void shouldRetrieveTokenByRfc() {
    repositoryProxy.tokenByRfc(RFC_TEST);
    verify(tokenRepo, times(1)).findFirstByRfc(any());
  }

  /**
   * Should delete token by rfc.
   */
  @Test
  public void shouldDeleteTokenByRfc() {
    repositoryProxy.tokenDeleteByRfc(RFC_TEST);
    verify(tokenRepo, times(1)).deleteByRfc(any());
  }

  /**
   * Should save packet.
   */
  @Test
  public void shouldSavePacket() {
    String packetId = UUID.randomUUID().toString();
    SatPacket packet = SatPacket.builder()
        .rfc(RFC_TEST)
        .status("5000")
        .packetId(packetId)
        .message("Ok Ready")
        .state(StateCode.READY.name()).build();

    repositoryProxy.savePacket(packet);
    verify(packetRepo, times(1)).save(any());
  }

  /**
   * Should retrieve packet by rfc and date end and date start.
   */
  @Test
  public void shouldRetrievePacketByRfcAndDateEndAndDateStart() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime endTime = now.plusDays(1);

    repositoryProxy.packetByRfcAndDateEndAndDateStart(RFC_TEST, now, endTime);
    verify(packetRepo, times(1)).findSatPacketByRfcAndDateEndAndDateStart(any(), any(), any());
  }

}