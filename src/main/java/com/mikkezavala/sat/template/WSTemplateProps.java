package com.mikkezavala.sat.template;

import com.mikkezavala.sat.domain.sat.SoapEndpoint;
import javax.xml.namespace.QName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WSTemplateProps {

  private String rfc;

  private QName qualifiedName;

  private SoapEndpoint endpoint;

}
