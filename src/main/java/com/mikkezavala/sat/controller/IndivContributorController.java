package com.mikkezavala.sat.controller;

import static com.mikkezavala.sat.util.Constant.KEY_STORE;
import static com.mikkezavala.sat.util.FielUtil.runScript;

import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.exception.FielException;
import com.mikkezavala.sat.exception.FielFileException;
import com.mikkezavala.sat.repository.SatClientRepository;
import com.mikkezavala.sat.service.sat.IndivContributorService;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The type Indiv contributor controller.
 */
@RestController
@RequestMapping(path = "/v1/persona-fisica")
public class IndivContributorController {

  private final IndivContributorService service;

  private final SatClientRepository satClientRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndivContributorController.class);

  /**
   * Instantiates a new Indiv contributor controller.
   *
   * @param service             the service
   * @param satClientRepository the sat client repository
   */
  public IndivContributorController(
      IndivContributorService service,
      SatClientRepository satClientRepository
  ) {
    this.service = service;
    this.satClientRepository = satClientRepository;
  }

  /**
   * Retrieve invoices.
   *
   * @param request the request
   * @return the invoices
   */
  @PostMapping("/download-received")
  public Invoices retrieve(@RequestBody RequestCfdi request) {
    LOGGER.info("Getting invoices for: {}", request.getRfc());
    return service.getReceptorInvoices(request);
  }

  /**
   * Handle file upload map.
   *
   * @param keyFile  the key file
   * @param certFile the cert file
   * @param rfc      the rfc
   * @param pass     the pass
   * @return the map
   */
  @PostMapping("/upload-key")
  public Map<String, String> handleFileUpload(
      @RequestParam("file-key") MultipartFile keyFile,
      @RequestParam("file-cert") MultipartFile certFile,
      @RequestParam("rfc") String rfc,
      @RequestParam("password") String pass
  ) {

    try {
      String uuid = UUID.randomUUID().toString();

      String keyLoc = KEY_STORE + rfc + "/" + uuid + "-key.key";
      FileUtils.writeByteArrayToFile(new File(keyLoc), keyFile.getBytes());

      String certLoc = KEY_STORE + rfc + "/" + uuid + "-cert.cer";
      FileUtils.writeByteArrayToFile(new File(certLoc), certFile.getBytes());

      String command = String.format(
          "./create-pfx.sh -r %s -c %s -k %s -p %s", rfc, certLoc, keyLoc, pass
      );

      int exitCode = runScript(command);
      FileUtils.forceDelete(new File(KEY_STORE + rfc));

      if (exitCode == 0) {
        SatClient key = saveKey(rfc, pass);
        if (Objects.isNull(key.id())) {
          throw new FielException("Failed creating KeyStore");
        }
      }
      return Collections.singletonMap("message", String.format("Keystore created for %s", rfc));
    } catch (FielFileException | IOException e) {
      throw new FielException("Failed handleFileUpload", e);
    }

  }

  /**
   * Save key sat client.
   *
   * @param rfc  the rfc
   * @param pass the pass
   * @return the sat client
   */
  public SatClient saveKey(String rfc, String pass) {

    try {

      SatClient satClient = new SatClient();

      if (!FileUtils.isDirectory(new File(KEY_STORE))) {
        FileUtils.forceMkdir(new File(KEY_STORE));
      }

      File keyFile = (new File(KEY_STORE + rfc + ".pfx"));

      satClient.keystore(keyFile.getAbsolutePath());
      satClient.passwordPlain(pass);
      satClient.rfc(rfc);
      satClientRepository.deleteAllByRfc(rfc);
      return satClientRepository.save(satClient);
    }catch (Exception e) {
      throw new FielFileException("Failed Saving keystore", e);
    }
  }

}