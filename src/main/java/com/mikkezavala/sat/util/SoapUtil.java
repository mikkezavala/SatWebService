package com.mikkezavala.sat.util;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Objects;
import javax.xml.soap.SOAPElement;

public class SoapUtil {

  private SoapUtil() {
    // Util Class
  }

  public static String getSignatureParts(String namespace, String element) {
    return String.format("{Content}{%s}%s", namespace, element);
  }

  public static SOAPElement getFirstNode(SOAPElement body) {
    for (Iterator<?> iterator = body.getChildElements(); iterator.hasNext(); ) {
      Object child = iterator.next();
      if (child instanceof SOAPElement) {
        return (SOAPElement) child;
      }
    }
    return null;
  }

  public static long getTokenDuration(SatToken satToken) {
    if (Objects.nonNull(satToken)) {
      ZonedDateTime expires = satToken.getExpiration();
      return Duration.between(ZonedDateTime.now(), expires).getSeconds();
    }

    return -1;
  }

}
