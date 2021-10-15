package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.util.Constant.DEFAULT_REQUEST_TYPE;
import static com.mikkezavala.sat.util.Constant.ENV_PREFIX;
import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.TIME_ZONE;
import static com.mikkezavala.sat.util.Constant.WSS_MESSAGE_SECURITY_NS;
import static com.mikkezavala.sat.util.Constant.WSS_SEC_EXT_NS;
import static com.mikkezavala.sat.util.Constant.WSS_TOKEN_PROFILE_NS;
import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;
import static com.mikkezavala.sat.util.SoapUtil.getFirstNode;
import static javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE;
import static javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1;

import com.mikkezavala.sat.domain.sat.auth.Auth;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.Download;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadRequest;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateDownload;
import com.mikkezavala.sat.exception.SatKeyStoreException;
import com.mikkezavala.sat.exception.SoapSecurityException;
import com.mikkezavala.sat.exception.SoapServiceException;
import com.mikkezavala.sat.repository.SatClientRepository;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
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
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * The type Soap service.
 */
@Component
public class SoapService {

  private final SoapHandler soapHandler;

  private final SatClientRepository repository;

  private final static int EXPIRES = 50000;

  private final static XMLSignatureFactory SIGN_FACTORY = XMLSignatureFactory.getInstance("DOM");

  /**
   * Instantiates a new Soap service.
   *
   * @param soapHandler the soap handler
   * @param repository  the repository
   */
  public SoapService(SoapHandler soapHandler, SatClientRepository repository) {
    this.soapHandler = soapHandler;
    this.repository = repository;
  }

  /**
   * Auth soap message.
   *
   * @param rfc the rfc
   * @return the soap message
   */
  public SOAPMessage auth(String rfc) {
    try {
      SOAPMessage message = soapHandler.createEnvelope();

      SOAPBody body = message.getSOAPBody();
      SOAPHeader header = message.getSOAPHeader();
      SOAPElement timestamp = createTimestampElement();
      SOAPElement security = createSecurityElement(header, rfc);

      security.addChildElement(timestamp);
      addSignature(security, timestamp, rfc);
      header.setPrefix(ENV_PREFIX);

      header.addNamespaceDeclaration("u", WSS_UTILITY_NS);
      body.addChildElement(soapHandler.createNodeOf(new Auth()));

      return message;
    } catch (SOAPException e) {
      throw new SoapServiceException("Unable to create auth message", e);
    }
  }

  /**
   * Request soap message.
   *
   * @param rfc       the rfc
   * @param dateStart the date start
   * @param dateEnd   the date end
   * @return the soap message
   */
  public SOAPMessage request(String rfc, ZonedDateTime dateStart, ZonedDateTime dateEnd) {

    try {
      SOAPMessage message = soapHandler.createEnvelope();
      RequestDownload requestDownload = new RequestDownload()
          .request(new Request()
              .rfcRequest(rfc)
              .rfcReceptor(rfc)
              .requestType(DEFAULT_REQUEST_TYPE)
              .dateEnd(FORMATTER.format(dateEnd))
              .dateStart(FORMATTER.format(dateStart))
          );

      SOAPBody body = message.getSOAPBody();
      SOAPElement downloadNode = soapHandler.createNodeOf(requestDownload);

      createDetachedSignature(getFirstNode(downloadNode), null, rfc);
      body.addChildElement(downloadNode);

      return message;
    } catch (SOAPException e) {
      throw new SoapServiceException("Cannot build request SOAPMessage", e);
    }
  }

  /**
   * Validation soap message.
   *
   * @param idSolicitud the id solicitud
   * @param rfc         the rfc
   * @return the soap message
   */
  public SOAPMessage validation(String idSolicitud, String rfc) {
    try {
      SOAPMessage message = soapHandler.createEnvelope();
      ValidateDownload validateDownload = new ValidateDownload().request(
          new Request().id(idSolicitud).rfcRequest(rfc)
      );

      SOAPBody body = message.getSOAPBody();
      SOAPElement verificaSolicitudNode = soapHandler.createNodeOf(validateDownload);

      createDetachedSignature(getFirstNode(verificaSolicitudNode), null, rfc);
      body.addChildElement(verificaSolicitudNode);

      return message;
    }catch (SOAPException e) {
      throw new SoapServiceException("Cannot build validation SOAPMessage", e);
    }
  }

  /**
   * Download soap message.
   *
   * @param idSolicitud the id solicitud
   * @param rfc         the rfc
   * @return the soap message
   */
  public SOAPMessage download(String idSolicitud, String rfc) {
    try {
      DownloadRequest request = new DownloadRequest();
      SOAPMessage message = soapHandler.createEnvelope();
      Download peticionMasiva = new Download();

      request.setIdPaquete(idSolicitud);
      request.setRfcSolicitante(rfc);
      peticionMasiva.setDownloadRequest(request);

      SOAPBody body = message.getSOAPBody();
      SOAPElement verificaSolicitudNode = soapHandler.createNodeOf(peticionMasiva);

      createDetachedSignature(getFirstNode(verificaSolicitudNode), null, rfc);
      body.addChildElement(verificaSolicitudNode);

      return message;
    }catch (SOAPException e) {
      throw new SoapServiceException("Cannot build download SOAPMessage", e);
    }
  }

  private SOAPElement createTimestampElement() throws SOAPException {
    String prefix = "u";
    SOAPElement timestamp = soapHandler.getSoapFactory()
        .createElement("Timestamp", prefix, WSS_UTILITY_NS);
    timestamp.addAttribute(
        soapHandler.getSoapFactory().createName("Id", prefix, WSS_UTILITY_NS), "_0");

    timestamp.addChildElement("Created", prefix).setValue(
        FORMATTER.format(ZonedDateTime.now().toInstant().atZone(ZoneId.of(TIME_ZONE)))
    );
    timestamp.addChildElement("Expires", prefix).setValue(FORMATTER
        .format(ZonedDateTime.now().plusMinutes(EXPIRES).toInstant().atZone(ZoneId.of(TIME_ZONE)))
    );

    return timestamp;
  }

  private SOAPElement createBinarySecurityTokenElement(String rfc){

    try {
      byte[] certByte = getCertificate(rfc).getEncoded();
      SOAPElement token = soapHandler.getSoapFactory()
          .createElement("BinarySecurityToken", "o", WSS_SEC_EXT_NS);
      token.setAttribute("u:Id", "X509Token");
      token.setAttribute("ValueType", WSS_TOKEN_PROFILE_NS);
      token.setAttribute("EncodingType", WSS_MESSAGE_SECURITY_NS);

      token.addTextNode(Base64.getEncoder().encodeToString(certByte));
      return token;
    } catch (Exception e) {
      throw new SoapSecurityException("Unable to decode certificate", e);
    }
  }

  private SOAPElement createSecurityElement(SOAPElement parent, String rfc) throws SOAPException {
    SOAPElement securityToken = createBinarySecurityTokenElement(rfc);
    SOAPElement securityElement = parent.addChildElement("Security", "o", WSS_SEC_EXT_NS);

    securityElement.addChildElement(securityToken);
    securityElement.addNamespaceDeclaration("o", WSS_SEC_EXT_NS);

    return securityElement;
  }

  private void addSignature(
      SOAPElement parent, SOAPElement element, String rfc
  ) throws SOAPException {
    createTokenReference(parent);
    createDetachedSignature(parent, element, rfc);
  }

  private void createTokenReference(SOAPElement element) throws SOAPException {

    SOAPElement tokenReference = element.addChildElement(
        "SecurityTokenReference", "o", WSS_SEC_EXT_NS
    );

    SOAPElement reference = tokenReference.addChildElement("Reference", "o");
    reference.setAttribute("URI", "#X509Token");
  }

  private void createDetachedSignature(SOAPElement parent, SOAPElement element, String rfc) {

    try {
      PrivateKey key = getKeyFormCert(rfc);
      boolean attachTopParent = Objects.nonNull(element);
      DOMSignContext signContext = new DOMSignContext(key, parent);
      DigestMethod digestMethod = SIGN_FACTORY.newDigestMethod(DigestMethod.SHA1, null);
      SignatureMethod signatureMethod = SIGN_FACTORY.newSignatureMethod(RSA_SHA1, null);

      Transform envTransform = SIGN_FACTORY
          .newTransform(EXCLUSIVE, (TransformParameterSpec) null);

      CanonicalizationMethod canonMethod = SIGN_FACTORY.newCanonicalizationMethod(
          EXCLUSIVE, (C14NMethodParameterSpec) null
      );

      List<Transform> transformList = Collections.singletonList(envTransform);
      List<Reference> refs = Collections.singletonList(
          SIGN_FACTORY.newReference(
              attachTopParent ? "#_0" : "", digestMethod, transformList, null, null
          )
      );

      SignedInfo signedInfo = SIGN_FACTORY.newSignedInfo(canonMethod, signatureMethod, refs);
      if (attachTopParent) {
        signContext.setBaseURI("");
        signContext.putNamespacePrefix(XMLSignature.XMLNS, "");
        signContext.setIdAttributeNS(element, WSS_UTILITY_NS, "Id");
      }

      XMLSignature xmlSignature = getXmlSignature(signedInfo, rfc);
      xmlSignature.sign(signContext);
    } catch (Exception e) {
      throw new SoapSecurityException("Unable to create detached signature", e);
    }
  }

  private XMLSignature getXmlSignature(SignedInfo signedInfo, String rfc) {

    KeyInfoFactory kif = KeyInfoFactory.getInstance();
    X509Certificate cert = (X509Certificate) getCertificate(rfc);

    final X509IssuerSerial issuer = kif.newX509IssuerSerial(
        cert.getSubjectX500Principal().getName(), cert.getSerialNumber()
    );

    List<Object> elements = new ArrayList<>();
    elements.add(cert);
    elements.add(issuer);

    X509Data x509Data = kif.newX509Data(elements);
    KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

    return SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

  }

  private Certificate getCertificate(String rfc) {
    try {
      SatClient client = repository.findSatClientByRfc(rfc);
      return getKeyStore(client).getCertificate("1");
    } catch (KeyStoreException | SatKeyStoreException e) {
      throw new SatKeyStoreException("Unable to retrieve certificate from keyStore", e);
    }
  }

  private PrivateKey getKeyFormCert(String rfc) {
    try {
      SatClient client = repository.findSatClientByRfc(rfc);
      return (PrivateKey) getKeyStore(client).getKey("1", client.getPasswordPlain().toCharArray());
    } catch (Exception e) {
      throw new SatKeyStoreException("Unable to retrieve key from keyStore", e);
    }
  }

  private KeyStore getKeyStore(SatClient client) {

    try {
      Resource cert = new FileSystemResource(client.getKeystore());

      KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(cert.getInputStream(), client.getPasswordPlain().toCharArray());

      return keystore;
    } catch (Exception e) {
      throw new SatKeyStoreException("Error reading keystore", e);
    }
  }
}
