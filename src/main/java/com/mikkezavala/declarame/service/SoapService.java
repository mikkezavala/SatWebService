package com.mikkezavala.declarame.service;

import static com.mikkezavala.declarame.util.XmlUtil.WSS_MESSAGE_SECURITY_NS;
import static com.mikkezavala.declarame.util.XmlUtil.WSS_SEC_EXT_NS;
import static com.mikkezavala.declarame.util.XmlUtil.WSS_TOKEN_PROFILE_NS;
import static com.mikkezavala.declarame.util.XmlUtil.WSS_UTILITY_NS;
import static javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE;

import com.mikkezavala.declarame.domain.sat.Autentica;
import com.mikkezavala.declarame.domain.sat.SolicitaDescarga;
import com.mikkezavala.declarame.domain.sat.Solicitud;
import com.mikkezavala.declarame.util.XmlUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.dom.DOMStructure;
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
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Component
public class SoapService {

  private final SOAPFactory soapFactory;

  private final MessageFactory messageFactory;

  private final DocumentBuilderFactory documentFactory;

  private Resource certFile = new ClassPathResource("XOJI.pfx");

  private final static String ENV_PREFIX = "s";

  private final static String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

  private static final Logger LOGGER = LoggerFactory.getLogger(SoapService.class);

  public SoapService(
      SOAPFactory soapFactory, MessageFactory messageFactory, DocumentBuilderFactory documentFactory
  ) {
    this.soapFactory = soapFactory;
    this.messageFactory = messageFactory;
    this.documentFactory = documentFactory;
  }

  public SOAPMessage autentica() throws Exception {
    SOAPMessage message = createEnvelope();

    SOAPBody body = message.getSOAPBody();
    SOAPHeader header = message.getSOAPHeader();
    SOAPElement timestamp = createTimestampElement();
    SOAPElement security = createSecurityElement(header);
    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

    security.addChildElement(timestamp);

    addSignature(security, timestamp);
    header.setPrefix(ENV_PREFIX);
    header.addNamespaceDeclaration("u", WSS_UTILITY_NS);

    body.addChildElement(createNodeOf(new Autentica()));
    body.addAttribute(envelope.createName("Id", "", WSS_UTILITY_NS), "Body");

    return message;
  }

  public SOAPMessage solicita() throws Exception {

    Solicitud solicitud = new Solicitud();
    SolicitaDescarga solicitaDescarga = new SolicitaDescarga();

    String endDate = FORMATTER.format(ZonedDateTime.now().toInstant().atZone(ZoneId.of("UTC")));
    String startDate = FORMATTER
        .format(ZonedDateTime.now().minusMonths(2).toInstant().atZone(ZoneId.of("UTC")));

    solicitud.setFechaFinal(endDate);
    solicitud.setTipoSolicitud("CFDI");
    solicitud.setFechaInicial(startDate);
    solicitud.setRfcEmisor("AUAC4601138F9");
    solicitud.setRfcReceptor("AUAC4601138F9");
    solicitud.setRfcSolicitante("AUAC4601138F9");
    solicitaDescarga.setSolicitud(solicitud);

    SOAPMessage message = createEnvelope();
    SOAPBody body = message.getSOAPBody();
    SOAPElement element = createNodeOf(solicitaDescarga);
    element.addAttribute(soapFactory.createName("Id", "des", WSS_UTILITY_NS), "_0");
    body.addChildElement(element);
    return message;
  }

  private SOAPMessage createEnvelope() throws Exception {

    SOAPMessage soapMessage = messageFactory.createMessage();

    SOAPBody soapBody = soapMessage.getSOAPBody();
    SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();

    soapBody.setPrefix(ENV_PREFIX);
    soapEnvelope.setPrefix(ENV_PREFIX);

    return soapMessage;
  }

  private SOAPElement createSecurityElement(SOAPElement parent) throws Exception {
    SOAPElement securityToken = createBinarySecurityTokenElement();
    SOAPElement securityElement = parent.addChildElement("Security", "o", WSS_SEC_EXT_NS);

    securityElement.addChildElement(securityToken);
    securityElement.addNamespaceDeclaration("o", WSS_SEC_EXT_NS);

    return securityElement;
  }

  private void addSignature(SOAPElement parent, SOAPElement element) throws Exception {
    PrivateKey key = getKeyFormCert();
    SOAPElement securityTokenReference = createTokenReference(parent);
    createDetachedSignature(parent, element, key, securityTokenReference);

  }

  private SOAPElement createTokenReference(SOAPElement element) throws SOAPException {

    SOAPElement tokenReference = element.addChildElement(
        "SecurityTokenReference", "o", WSS_SEC_EXT_NS
    );

    SOAPElement reference = tokenReference.addChildElement("Reference", "o");
    reference.setAttribute("URI", "#X509Token");

    return tokenReference;
  }

  private void createDetachedSignature(
      SOAPElement parent, SOAPElement element, PrivateKey key, SOAPElement securityTokenReference
  ) throws Exception {

    XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
    DOMSignContext signContext = new DOMSignContext(key, parent);

    DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);
    Transform envTransform = factory.newTransform(EXCLUSIVE, (TransformParameterSpec) null);
    SignatureMethod signatureMethod = factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
    CanonicalizationMethod canonicalizationMethod = factory.newCanonicalizationMethod(
        EXCLUSIVE, (C14NMethodParameterSpec) null
    );

    List<Transform> transformList = Collections.singletonList(envTransform);
    List<Reference> refs = Collections.singletonList(
        factory.newReference("#_0", digestMethod, transformList, null, null)
    );

    SignedInfo signedInfo = factory.newSignedInfo(canonicalizationMethod, signatureMethod, refs);

    signContext.setBaseURI("");
    signContext.putNamespacePrefix(XMLSignature.XMLNS, "");
    signContext.setIdAttributeNS(element, WSS_UTILITY_NS, "Id");

    KeyInfoFactory keyFactory = KeyInfoFactory.getInstance();
    DOMStructure domKeyInfo = new DOMStructure(securityTokenReference);
    KeyInfo keyInfo = keyFactory.newKeyInfo(Collections.singletonList(domKeyInfo));
    XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);

    signature.sign(signContext);
  }

//  private void createSignature(PrivateKey privateKey, SOAPElement element) {
//    try {
//
//      DOMSignContext signContext = new DOMSignContext(privateKey, element);
//      XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
//
//      List<Transform> transformList = new LinkedList<>();
//      transformList.add(signatureFactory.newTransform(EXCLUSIVE, (TransformParameterSpec) null));
//
//      Node child = findFirstElementChild(element);
//      ((Element) child).setIdAttribute("Id", true);
//
//      String id = child.getAttributes().getNamedItem("Id").getNodeValue();
//      String uri = String.format("#%s", id);
//      Reference reference = signatureFactory.newReference(uri,
//          signatureFactory.newDigestMethod(DigestMethod.SHA1, null), transformList, null, null);
//
//      SignedInfo signedInfo = signatureFactory
//          .newSignedInfo(signatureFactory.newCanonicalizationMethod(
//              CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), signatureFactory
//                  .newSignatureMethod(SignatureMethod.RSA_SHA1, null),
//              Collections.singletonList(reference));
//
//      KeyInfoFactory kif = signatureFactory.getKeyInfoFactory();
//      X509Data x509Data = kif.newX509Data(Collections.singletonList(certificateChain[0]));
//      KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));
//
//      XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
//
//      xmlSignature.sign(dsc);
//
//      return node;
//    } catch (Exception ex) {
//      throw new IllegalArgumentException("Erro ao assinar XML.", ex);
//    }
//  }

  private Certificate getCertificate() throws Exception {

    String password = "12345678a";
    InputStream is = certFile.getInputStream();
    KeyStore keystore = KeyStore.getInstance("pkcs12", "SunJSSE");

    keystore.load(is, password.toCharArray());
    return keystore.getCertificate("1");
  }

  private PrivateKey getKeyFormCert() throws Exception {

    String password = "12345678a";
    InputStream is = certFile.getInputStream();
    KeyStore keystore = KeyStore.getInstance("pkcs12");

    keystore.load(is, password.toCharArray());
    return (PrivateKey) keystore.getKey("1", password.toCharArray());
  }

  public String soapMessageString(SOAPMessage soapMessage)
      throws SOAPException, TransformerException {

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer transformer = transformerFactory.newTransformer();

    // Format it
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    final Source soapContent = soapMessage.getSOAPPart().getContent();

    final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    final StreamResult result = new StreamResult(streamOut);
    transformer.transform(soapContent, result);

    return streamOut.toString();
  }

  public void outputSOAPMessageToFile(SOAPMessage soapMessage)
      throws SOAPException, IOException {

    File outputFile = new File("./output.soap");
    FileOutputStream fos = new FileOutputStream(outputFile);
    soapMessage.writeTo(fos);
    fos.close();

  }

  public String callTheWebServiceFromFile() throws IOException, SOAPException {
    File soapFile = new File("./output.soap");
    FileInputStream fis = new FileInputStream(soapFile);
    StreamSource ss = new StreamSource(fis);

    SOAPMessage msg = MessageFactory.newInstance().createMessage();
    SOAPPart soapPart = msg.getSOAPPart();

    soapPart.setContent(ss);
    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
    SOAPConnection soapConnection = soapConnectionFactory.createConnection();

    String soapAction = "http://DescargaMasivaTerceros.gob.mx/IAutenticacion/Autentica";
    msg.getMimeHeaders().addHeader("SOAPAction", soapAction);

    String soapEndpointUrl = "https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/Autenticacion/Autenticacion.svc";
    SOAPMessage resp = soapConnection.call(msg, soapEndpointUrl);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    resp.writeTo(os);
    fis.close();
    soapConnection.close();

    return os.toString();
  }

  private <T> SOAPElement createNodeOf(T request) throws Exception {
    String xml = XmlUtil.serialize(request, true);
    InputSource source = new InputSource(new StringReader(xml));
    DocumentBuilder builder = documentFactory.newDocumentBuilder();
    Document doc = builder.parse(source);

    return soapFactory.createElement(doc.getDocumentElement());
  }

  private SOAPElement createTimestampElement() throws SOAPException {
    String prefix = "u";
    SOAPElement timestamp = soapFactory.createElement("Timestamp", prefix, WSS_UTILITY_NS);
    timestamp.addAttribute(soapFactory.createName("Id", prefix, WSS_UTILITY_NS), "_0");

    timestamp.addChildElement("Created", prefix).setValue(
        FORMATTER.format(ZonedDateTime.now().toInstant().atZone(ZoneId.of("UTC")))
    );
    timestamp.addChildElement("Expires", prefix).setValue(FORMATTER
        .format(ZonedDateTime.now().plusMinutes(30).toInstant().atZone(ZoneId.of("UTC")))
    );

    return timestamp;
  }

  private SOAPElement createBinarySecurityTokenElement() throws Exception {

    byte[] certByte = getCertificate().getEncoded();
    SOAPElement token = soapFactory.createElement("BinarySecurityToken", "o", WSS_SEC_EXT_NS);
    token.setAttribute("u:Id", "X509Token");
    token.setAttribute("ValueType", WSS_TOKEN_PROFILE_NS);
    token.setAttribute("EncodingType", WSS_MESSAGE_SECURITY_NS);

    token.addTextNode(Base64.getEncoder().encodeToString(certByte));
    return token;
  }
}
