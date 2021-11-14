package com.mikkezavala.sat.service.sat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.domain.client.registered.RequestCFDI;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Result;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

/**
 * The type Request download service test.
 */
class RequestDownloadServiceTest extends AbstractSatServiceTest {

  @InjectMocks
  private RequestDownloadService service;

  /**
   * Should return download response validation.
   */
  @Test
  public void shouldReturnDownloadResponseValidation() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime end = now.plusDays(10);
    String uuid = UUID.randomUUID().toString();

    RequestCFDI request = RequestCFDI.builder().rfc(RFC_TEST).dateStart(now).dateEnd(end).build();

    Response response = new Response().result(
        new Result().requestId(uuid).message("ACCEPTED").status("5000")
    );

    when(
        template.marshalSendAndReceiveWithProps(any(), any(RequestDownload.class))
    ).thenReturn(response);

    Response res = service.getRequest(request);

    assertThat(res).isNotNull();
    assertThat(res.result()).isNotNull();
    assertThat(res.result().status()).isEqualTo("5000");
    assertThat(res.result().requestId()).isEqualTo(uuid);
    assertThat(res.result().message()).isEqualTo("ACCEPTED");
  }

  /**
   * Should return empty response when template failure.
   */
  @Test
  public void shouldReturnEmptyResponseWhenTemplateFailure() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime end = now.plusDays(10);

    RequestCFDI request = RequestCFDI.builder().rfc(RFC_TEST).dateStart(now).dateEnd(end).build();

    when(
        template.marshalSendAndReceiveWithProps(any(), any(RequestDownload.class))
    ).thenThrow(new RuntimeException("Exploded"));

    Response res = service.getRequest(request);

    assertThat(res).isNotNull();
    assertThat(res.result()).isNull();
  }

}