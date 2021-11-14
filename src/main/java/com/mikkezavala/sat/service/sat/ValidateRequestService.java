package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;
import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;

import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.template.SatWSTemplate;
import com.mikkezavala.sat.template.WSTemplateProps;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

/**
 * The type Validate request service.
 */
@Service
public class ValidateRequestService {

  private final SatWSTemplate template;

  private final ClientInterceptor interceptor;

  private static final String LOGGER_PREFIX = "[SAT VERIFICATION]: {} {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidateRequestService.class);

  /**
   * Instantiates a new Validate request service.
   *
   * @param template    the template
   * @param interceptor default interceptor
   */
  public ValidateRequestService(SatWSTemplate template, ClientInterceptor interceptor) {
    this.template = template;
    this.interceptor = interceptor;
  }

  /**
   * Gets validation.
   *
   * @param rfc       the rfc
   * @param requestId the request id
   * @return the validation
   */
  public ValidateResponse getValidation(String rfc, String requestId) {

    template.overrideInterceptors(new ClientInterceptor[]{interceptor});

    try {

      WSTemplateProps props = WSTemplateProps.builder()
          .rfc(rfc)
          .endpoint(VALIDA_DESCARGA)
          .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "VerificaSolicitudDescarga")).build();

      ValidateDownload validateDownload = new ValidateDownload().request(
          new Request().id(requestId).rfcRequest(rfc)
      );

      return (ValidateResponse) template.marshalSendAndReceiveWithProps(props, validateDownload);

    } catch (RuntimeException e) {
      LOGGER.error(LOGGER_PREFIX, "Error Validating", e.getMessage());
    }
    return new ValidateResponse();
  }

}
