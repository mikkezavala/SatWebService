package com.mikkezavala.sat.template;

import com.mikkezavala.sat.domain.sat.SoapEndpoint;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.support.MarshallingUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.support.TransportUtils;

/**
 * The type Sat web service template.
 */
@Component
public class SatWebServiceTemplate extends WebServiceTemplate implements SatWSTemplate {

  /**
   * The constant WS_TEMPLATE.
   */
  public static final String WS_TEMPLATE = "WS_TEMPLATE";

  /**
   * Instantiates a new Sat web service template.
   *
   * @param marshaller  the marshaller
   */
  public SatWebServiceTemplate(Marshaller marshaller) {
    super(marshaller);
  }

  @Override
  public Marshaller getMarshaller() {
    return super.getMarshaller();
  }

  @Override
  public void overrideInterceptors(ClientInterceptor[] interceptors) {
    super.setInterceptors(interceptors);
  }

  @Override
  public Object marshalSendAndReceiveWithProps(
      final WSTemplateProps props, final Object requestPayload
  ) {
    return sendAndReceiveWithProps(props, request -> {
      if (requestPayload != null) {
        Marshaller marshaller = getMarshaller();
        if (marshaller == null) {
          throw new IllegalStateException(
              "No marshaller registered. Check configuration of WebServiceTemplate."
          );
        }
        MarshallingUtils.marshal(marshaller, requestPayload, request);
        new SoapActionCallback(props.getEndpoint().getAction()).doWithMessage(request);
      }
    }, response -> {
      Unmarshaller unmarshaller = getUnmarshaller();
      if (unmarshaller == null) {
        throw new IllegalStateException(
            "No unmarshaller registered. Check configuration of WebServiceTemplate."
        );
      }
      return MarshallingUtils.unmarshal(unmarshaller, response);
    });
  }

  @Override
  public <T> T sendAndReceiveWithProps(
      WSTemplateProps props,
      WebServiceMessageCallback requestCallback,
      WebServiceMessageExtractor<T> responseExtractor
  ) {
    SoapEndpoint endpoint = props.getEndpoint();
    Assert.notNull(responseExtractor, "'responseExtractor' must not be null");
    Assert.hasLength(endpoint.getEndpoint(), "'uri' must not be empty");
    TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
    WebServiceConnection connection = null;
    try {
      connection = createConnection(URI.create(endpoint.getEndpoint()));
      TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));
      MessageContext messageContext = new DefaultMessageContext(getMessageFactory());
      SoapMessage soapMessage = ((SoapMessage) messageContext.getRequest());
      soapMessage.setSoapAction(endpoint.getAction());


      if (Objects.nonNull(props.getQualifiedName()) || StringUtils.isNotEmpty(props.getRfc())) {
        messageContext.setProperty(WS_TEMPLATE, props);
      }
      return doSendAndReceive(messageContext, connection, requestCallback, responseExtractor);
    } catch (Exception ex) {
      throw new WebServiceTransportException("Could not use transport: " + ex.getMessage());
    } finally {
      TransportUtils.closeConnection(connection);
      TransportContextHolder.setTransportContext(previousTransportContext);
    }
  }

}
