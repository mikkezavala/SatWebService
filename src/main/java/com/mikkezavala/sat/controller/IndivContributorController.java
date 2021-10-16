package com.mikkezavala.sat.controller;

import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.service.sat.IndivContributorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Indiv contributor controller.
 */
@RestController
@RequestMapping(path = "/v1/persona-fisica")
public class IndivContributorController {

  private final IndivContributorService service;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndivContributorController.class);

  /**
   * Instantiates a new Indiv contributor controller.
   *
   * @param service the service
   */
  public IndivContributorController(IndivContributorService service) {
    this.service = service;
  }

  /**
   * Retrieve object.
   *
   * @param request the request
   * @return the object
   */
  @PostMapping("/download-received")
  public Invoices retrieve(@RequestBody RequestCfdi request) {
    LOGGER.info("Getting invoices for: {}", request.getRfc());
    return service.getReceptorInvoices(request);
  }

}
