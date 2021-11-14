package com.mikkezavala.sat.service.sat;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResult;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

/**
 * The type Validate request service test.
 */
class ValidateRequestServiceTest extends AbstractSatServiceTest {

  @InjectMocks
  private ValidateRequestService service;

  /**
   * Should return packages on validation.
   */
  @Test
  public void shouldReturnPackagesOnValidation() {

    String requestId = UUID.randomUUID().toString();
    ValidateResponse responseMock = new ValidateResponse().result(
        new ValidateResult()
            .cfdiCount(1)
            .status("5000")
            .message("Success")
            .state(StateCode.READY)
            .IdsPaquetes(Collections.singletonList(UUID.randomUUID().toString()))
    );

    when(
        template.marshalSendAndReceiveWithProps(any(), any(ValidateDownload.class))
    ).thenReturn(responseMock);

    ValidateResponse response = service.getValidation(RFC_TEST, requestId);

    assertThat(response).isNotNull();
    assertThat(response.result()).isNotNull();
    assertThat(response.result().cfdiCount()).isEqualTo(1);
    assertThat(response.result().IdsPaquetes()).hasSize(1);
    assertThat(response.result().status()).isEqualTo("5000");
    assertThat(response.result().message()).isEqualTo("Success");
    assertThat(response.result().state()).isEqualTo(StateCode.READY);

  }

  /**
   * Should return empty response when template failure.
   */
  @Test
  public void shouldReturnEmptyResponseWhenTemplateFailure() {
    String requestId = UUID.randomUUID().toString();

    when(
        template.marshalSendAndReceiveWithProps(any(), any(ValidateDownload.class))
    ).thenThrow(new RuntimeException("Exploded"));

    ValidateResponse res = service.getValidation(RFC_TEST, requestId);

    assertThat(res).isNotNull();
    assertThat(res.result()).isNull();
  }

}