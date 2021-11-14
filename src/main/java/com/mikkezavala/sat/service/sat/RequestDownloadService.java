package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.util.Constant.DEFAULT_REQUEST_TYPE;
import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;

import com.mikkezavala.sat.domain.client.registered.RequestCFDI;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.template.SatWSTemplate;
import com.mikkezavala.sat.template.WSTemplateProps;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;


/**
 * The type Request download service.
 */
@Service
public class RequestDownloadService {

  private final SatWSTemplate template;

  private final ClientInterceptor interceptor;

  private static final String LOGGER_PREFIX = "[SAT REQUEST DOWNLOAD]: {} {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestDownloadService.class);

  /**
   * Instantiates a new Request download service.
   *
   * @param template    the template
   * @param interceptor default interceptor
   */
  public RequestDownloadService(SatWSTemplate template, ClientInterceptor interceptor) {
    this.template = template;
    this.interceptor = interceptor;
  }

  /**
   * Gets request.
   *
   * @param request the request
   * @return the request
   */
  public Response getRequest(RequestCFDI request) {

    String rfc = request.getRfc();
    template.overrideInterceptors(new ClientInterceptor[]{interceptor});

    try {

      WSTemplateProps props = WSTemplateProps.builder()
          .rfc(rfc)
          .endpoint(SOLICITA_DESCARGA)
          .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

      RequestDownload req = new RequestDownload().request(new Request()
          .rfcRequest(rfc)
          .rfcReceptor(rfc)
          .requestType(DEFAULT_REQUEST_TYPE)
          .dateEnd(request.getDateEnd().format(FORMATTER))
          .dateStart(request.getDateStart().format(FORMATTER))
      );

      return (Response) template.marshalSendAndReceiveWithProps(props, req);

    } catch (RuntimeException e) {
      LOGGER.error(LOGGER_PREFIX, "Error Returning Download Validation", e.getMessage());
    }
    return new Response();
  }


}
