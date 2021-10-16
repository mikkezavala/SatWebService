package com.mikkezavala.sat.util;

import java.time.format.DateTimeFormatter;

public class Constant {

  public final static String ENV_PREFIX = "s";

  public final static String TIME_ZONE = "UTC";

  public final static String DEFAULT_REQUEST_TYPE = "CFDI";

  public final static String KEY_STORE = "./keystore/";

  public final static String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

  public final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

  public final static String SAT_DESCARGA_NS = "http://DescargaMasivaTerceros.gob.mx";

  public final static String ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/";

  public final static String WSS_SEC_EXT_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  public final static String WSS_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

  public final static String WSS_TOKEN_PROFILE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

  public final static String WSS_MESSAGE_SECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

}
