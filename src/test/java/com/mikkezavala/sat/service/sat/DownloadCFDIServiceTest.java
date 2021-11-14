package com.mikkezavala.sat.service.sat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.domain.sat.cfdi.individual.download.Download;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

/**
 * The type Download cfdi service test.
 */
class DownloadCFDIServiceTest extends AbstractSatServiceTest {

  @InjectMocks
  private DownloadCFDIService service;

  /**
   * Should return encoded packet download.
   */
  @Test
  public void shouldReturnEncodedPacketDownload() {
    String packetId = UUID.randomUUID().toString();
    String packet = Base64.getEncoder().encodeToString("TEST".getBytes(StandardCharsets.UTF_8));

    DownloadResponse responseMock = new DownloadResponse().paquete(packet);

    when(
        template.marshalSendAndReceiveWithProps(any(), any(Download.class))
    ).thenReturn(responseMock);

    DownloadResponse response = service.getDownload(RFC_TEST, packetId);

    assertThat(response).isNotNull();
    assertThat(response.paquete()).isEqualTo(packet);
  }

  /**
   * Should return empty response when template failure.
   */
  @Test
  public void shouldReturnEmptyResponseWhenTemplateFailure() {
    String requestId = UUID.randomUUID().toString();

    when(
        template.marshalSendAndReceiveWithProps(any(), any(Download.class))
    ).thenThrow(new RuntimeException("Exploded"));

    DownloadResponse res = service.getDownload(RFC_TEST, requestId);

    assertThat(res).isNotNull();
    assertThat(res.paquete()).isNull();
  }

}