package com.mikkezavala.sat.controller;

import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.service.sat.IndividualContributorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Individual contributor controller.
 */
@RestController
@RequestMapping(path = "/v1/persona-fisica")
public class IndividualContributorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      IndividualContributorController.class);
  private final IndividualContributorService service;

  /**
   * Instantiates a new Individual contributor controller.
   *
   * @param service the service
   */
  public IndividualContributorController(IndividualContributorService service) {
    this.service = service;
  }

  /**
   * Retrieve object.
   *
   * @param request the request
   * @return the object
   */
  @PostMapping("/download-received")
  public Object retrieve(@RequestBody RequestCfdi request) {
    return service.getReceptorInvoices(request);
  }

}
