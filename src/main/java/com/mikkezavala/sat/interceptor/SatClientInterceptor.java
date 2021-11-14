package com.mikkezavala.sat.interceptor;

import static com.mikkezavala.sat.template.SatWebServiceTemplate.WS_TEMPLATE;
import static com.mikkezavala.sat.util.SoapUtil.getTokenDuration;
import static javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE;
import static javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.exception.SatKeyStoreException;
import com.mikkezavala.sat.exception.SoapSecurityException;
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.template.WSTemplateProps;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

@Component
public class SatClientInterceptor implements ClientInterceptor {

  private final SatRepository repository;

  private static final String SIGN_FACTORY_REFERENCE = "";

  private static final String TOKEN_FORMAT = "WRAP access_token=\"%s\"";

  private static final String LOGGER_PREFIX = "[SAT CLIENT INTERCEPTOR]: {} - {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(SatClientInterceptor.class);

  public final static XMLSignatureFactory SIGN_FACTORY = XMLSignatureFactory.getInstance("DOM");


  public SatClientInterceptor(SatRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
    WebServiceMessage message = messageContext.getRequest();
    SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
    WSTemplateProps props = (WSTemplateProps) messageContext.getProperty(WS_TEMPLATE);

    try {
      SatToken token = repository.tokenByRfc(props.getRfc());
      SatClient client = repository.satClientByRfc(props.getRfc());

      LOGGER.info(
          LOGGER_PREFIX,
          props.getEndpoint().getAction(),
          String.format("Token Valid for: %s", getTokenDuration(token))
      );

      appendToken(token.getToken());
      Iterator<Node> itNodes = soapMessage.getSOAPBody().getChildElements(props.getQualifiedName());

      while (itNodes.hasNext()) {
        Node node = itNodes.next();
        Node parent = (Node) node.getFirstChild();
        createDetachedSignature(parent, client);
      }
    } catch (SOAPException e) {
      LOGGER.error("Error signing content: ", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
    LOGGER.info(LOGGER_PREFIX, "SOAP Response handling", "");
    return true;
  }

  @Override
  public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
    LOGGER.error(LOGGER_PREFIX, "SOAP Fault handling", "");
    return false;
  }

  @Override
  public void afterCompletion(MessageContext messageContext, Exception ex)
      throws WebServiceClientException {
    LOGGER.info(LOGGER_PREFIX, "SOAP Completion handling", "");
  }

  private void createDetachedSignature(Node parent, SatClient client) {

    try {
      KeyStore keyStore = getKeyStore(client);
      Certificate certificate = keyStore.getCertificate(client.rfc());
      PrivateKey key = (PrivateKey) keyStore.getKey(
          client.rfc(), client.passwordPlain().toCharArray()
      );

      DOMSignContext signContext = new DOMSignContext(key, parent);
      DigestMethod digestMethod = SIGN_FACTORY.newDigestMethod(DigestMethod.SHA1, null);
      SignatureMethod signatureMethod = SIGN_FACTORY.newSignatureMethod(RSA_SHA1, null);
      Transform envTransform = SIGN_FACTORY.newTransform(EXCLUSIVE, (TransformParameterSpec) null);

      CanonicalizationMethod canonMethod = SIGN_FACTORY.newCanonicalizationMethod(
          EXCLUSIVE, (C14NMethodParameterSpec) null
      );

      List<Transform> transformList = Collections.singletonList(envTransform);
      List<Reference> refs = Collections.singletonList(
          SIGN_FACTORY.newReference(SIGN_FACTORY_REFERENCE, digestMethod, transformList, null, null)
      );

      SignedInfo signedInfo = SIGN_FACTORY.newSignedInfo(canonMethod, signatureMethod, refs);

      XMLSignature xmlSignature = getXmlSignature(signedInfo, certificate);
      xmlSignature.sign(signContext);
    } catch (Exception e) {
      throw new SoapSecurityException("Unable to create detached signature", e);
    }
  }

  private XMLSignature getXmlSignature(SignedInfo signedInfo, Certificate certificate) {

    KeyInfoFactory kif = KeyInfoFactory.getInstance();
    X509Certificate cert = (X509Certificate) certificate;
    final X509IssuerSerial issuer = getIssuerSerial(cert);

    List<Object> elements = new ArrayList<>();
    elements.add(cert);
    elements.add(issuer);

    X509Data x509Data = kif.newX509Data(elements);
    KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

    return SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

  }

  private X509IssuerSerial getIssuerSerial(X509Certificate cert) {
    KeyInfoFactory kif = KeyInfoFactory.getInstance();
    return kif.newX509IssuerSerial(
        cert.getSubjectX500Principal().getName(), cert.getSerialNumber()
    );
  }

  private KeyStore getKeyStore(SatClient client) {
    try {
      KeyStore keystore = KeyStore.getInstance("PKCS12");
      Resource cert = new FileSystemResource(client.keystore());
      keystore.load(cert.getInputStream(), client.passwordPlain().toCharArray());
      return keystore;
    } catch (Exception e) {
      throw new SatKeyStoreException("Error reading keystore", e);
    }
  }

  private void appendToken(String token) {
    try {

      TransportContext context = TransportContextHolder.getTransportContext();
      if (Objects.nonNull(context)) {
        HttpUrlConnection connection = (HttpUrlConnection) context.getConnection();
        connection.addRequestHeader("Authorization", String.format(TOKEN_FORMAT, token));
      }
    } catch (IOException e) {
      LOGGER.error("Error Intercepting and adding token");
    }
  }

}

