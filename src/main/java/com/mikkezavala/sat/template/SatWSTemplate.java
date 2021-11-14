package com.mikkezavala.sat.template;

import org.springframework.oxm.Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

/**
 * The interface Sat ws template.
 */
public interface SatWSTemplate {

  /**
   * Gets marshaller.
   *
   * @return the marshaller
   */
  Marshaller getMarshaller();

  /**
   * Override interceptors.
   *
   * @param interceptors the interceptors
   */
  void overrideInterceptors(ClientInterceptor[] interceptors);

  /**
   * Send and receive with props t.
   *
   * @param <T>               the type parameter
   * @param props             the props
   * @param requestCallback   the request callback
   * @param responseExtractor the response extractor
   * @return the t
   */
  <T> T sendAndReceiveWithProps(
      WSTemplateProps props,
      WebServiceMessageCallback requestCallback,
      WebServiceMessageExtractor<T> responseExtractor
  );

  /**
   * Marshal send and receive with props object.
   *
   * @param props           the props
   * @param requestPayload  the request payload
   * @return the object
   */
  Object marshalSendAndReceiveWithProps(final WSTemplateProps props, final Object requestPayload);
}
