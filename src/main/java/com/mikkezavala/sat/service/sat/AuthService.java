package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.util.Constant.WSS_SEC_EXT_NS;
import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;
import static com.mikkezavala.sat.util.SoapUtil.getSignatureParts;
import static org.apache.wss4j.common.WSS4JConstants.SIG_LN;
import static org.apache.wss4j.common.WSS4JConstants.TIMESTAMP_TOKEN_LN;
import static org.springframework.ws.support.MarshallingUtils.unmarshal;

import com.mikkezavala.sat.domain.sat.auth.Auth;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.auth.Security;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.template.SatWSTemplate;
import com.mikkezavala.sat.template.WSTemplateProps;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.support.MarshallingUtils;

@Service
public class AuthService {

  private final SatWSTemplate template;

  private final SatRepository repository;

  private static final String CRYPTO_STORE_FILE = "org.apache.ws.security.crypto.merlin.file";

  private static final String CRYPTO_STORE_TYPE = "org.apache.ws.security.crypto.merlin.keystore.type";

  private static final String CRYPTO_STORE_PASS = "org.apache.ws.security.crypto.merlin.keystore.password";

  private static final String LOGGER_PREFIX = "[SAT AUTH TOKEN]: {} - {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

  public AuthService(SatWSTemplate template, SatRepository repository) {
    this.template = template;
    this.repository = repository;
  }

  public AuthResponse getAuth(String rfc) {

    SatClient satClient = repository.satClientByRfc(rfc);
    Jaxb2Marshaller marshaller = (Jaxb2Marshaller) template.getMarshaller();

    try {

      template.overrideInterceptors(new ClientInterceptor[]{interceptor(satClient)});
      return template.sendAndReceiveWithProps(
          WSTemplateProps.builder().rfc(rfc).endpoint(AUTENTICA).build(),
          message -> MarshallingUtils.marshal(marshaller, new Auth(), message),
          message -> {
            Security security = getSecurityFromHeader(((SoapMessage) message).getSoapHeader());
            AuthResponse authResponse = (AuthResponse) unmarshal(marshaller, message);
            authResponse.setTimestamp(security.getTimestamp());
            return authResponse;
          }
      );
    } catch (RuntimeException e) {
      LOGGER.error(LOGGER_PREFIX, "Error Authing customer", e.getMessage());
    }
    return new AuthResponse();
  }

  private Wss4jSecurityInterceptor interceptor(SatClient client) {

    Crypto crypto = getCrypto(client);

    if (Objects.nonNull(crypto)) {
      final Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

      interceptor.setSecurementSignatureCrypto(crypto);
      interceptor.setSecurementActions(String.join(" ", SIG_LN, TIMESTAMP_TOKEN_LN));

      interceptor.setSecurementUsername(client.rfc());
      interceptor.setSecurementPassword(client.passwordPlain());
      interceptor.setSecurementSignatureParts(getSignatureParts(WSS_UTILITY_NS, "Timestamp"));

      interceptor.setSecurementSignatureKeyIdentifier("DirectReference");

      interceptor.setValidateResponse(false);
      interceptor.setValidateRequest(false);

      return interceptor;
    }

    throw new RuntimeException("Crypto Store mis-configured");
  }

  // ToDo: Crypto Store is global, syncing for now
  private Crypto getCrypto(SatClient satClient) {
    try {
      synchronized (this) {
        Properties props = new Properties();
        props.setProperty(CRYPTO_STORE_TYPE, "PKCS12");
        props.setProperty(CRYPTO_STORE_PASS, satClient.passwordPlain());
        Resource keyLocation = new FileSystemResource(satClient.keystore());
        props.setProperty(CRYPTO_STORE_FILE, keyLocation.getFile().getAbsolutePath());
        return CryptoFactory.getInstance(props);
      }
    } catch (WSSecurityException | IOException e) {
      LOGGER.error(LOGGER_PREFIX, "Error Fetching keyStore file for Customer", e.getMessage());
    }

    return null;
  }


  private Security getSecurityFromHeader(SoapHeader header) {
    Security timestamp = new Security();
    Jaxb2Marshaller marshaller = (Jaxb2Marshaller) template.getMarshaller();

    Iterator<SoapHeaderElement> it = header.examineHeaderElements(
        new QName(WSS_SEC_EXT_NS, "Security", "o")
    );

    while (it.hasNext()) {
      SoapHeaderElement el = it.next();
      timestamp = (Security) marshaller.unmarshal(el.getSource());
    }

    return timestamp;
  }

}
