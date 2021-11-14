package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.DESCARGA_MASIVA;
import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;

import com.mikkezavala.sat.domain.sat.cfdi.individual.download.Download;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadRequest;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import com.mikkezavala.sat.template.SatWSTemplate;
import com.mikkezavala.sat.template.WSTemplateProps;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;


/**
 * The type Download cfdi service.
 */
@Service
public class DownloadCFDIService {

  private final SatWSTemplate template;

  private final ClientInterceptor interceptor;

  private static final String LOGGER_PREFIX = "[SAT DOWNLOAD CFDI]: {} {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadCFDIService.class);

  /**
   * Instantiates a new Download cfdi service.
   *
   * @param template    the template
   * @param interceptor default Interceptor
   */
  public DownloadCFDIService(SatWSTemplate template, ClientInterceptor interceptor) {
    this.template = template;
    this.interceptor = interceptor;
  }

  /**
   * Gets download.
   *
   * @param rfc      the rfc
   * @param packetId the packet id
   * @return the download
   */
  public DownloadResponse getDownload(String rfc, String packetId) {

    template.overrideInterceptors(new ClientInterceptor[]{interceptor});

    try {

      WSTemplateProps props = WSTemplateProps.builder()
          .rfc(rfc)
          .endpoint(DESCARGA_MASIVA)
          .qualifiedName(
              new QName(SAT_DESCARGA_MASIVA_NS, "PeticionDescargaMasivaTercerosEntrada")
          ).build();

      Download validateDownload = new Download().downloadRequest(
          new DownloadRequest().RfcSolicitante(rfc).IdPaquete(packetId)
      );

      return (DownloadResponse) template.marshalSendAndReceiveWithProps(props, validateDownload);

    } catch (RuntimeException e) {
      LOGGER.error(LOGGER_PREFIX, "Error Downloading CFDI", e.getMessage());
    }
    return new DownloadResponse();
  }

}
