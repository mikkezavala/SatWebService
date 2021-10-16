package com.mikkezavala.sat.controller;

import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.KEY_STORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Complemento;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Issuer;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Payment;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Payments;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Receptor;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.exception.FielException;
import com.mikkezavala.sat.exception.FielFileException;
import com.mikkezavala.sat.repository.SatClientRepository;
import com.mikkezavala.sat.service.sat.IndivContributorService;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Indiv contributor controller test.
 */
@ExtendWith(SpringExtension.class)
public class IndivContributorControllerTest extends TestBase {

  @Mock
  private IndivContributorService service;

  @Mock
  private SatClientRepository satClientRepository;

  @InjectMocks
  private IndivContributorController controller;

  /**
   * Should return invoices.
   */
  @Test
  public void shouldReturnInvoices() {
    when(service.getReceptorInvoices(any())).thenReturn(buildMockInvoices());
    RequestCfdi requestCfd = new RequestCfdi();
    requestCfd.setRfc(RFC_TEST);
    requestCfd.setDateStart(ZonedDateTime.now());
    requestCfd.setDateStart(ZonedDateTime.now().plusDays(2));
    Invoices invoices = controller.retrieve(requestCfd);

    assertThat(invoices.getInvoices()).hasSize(1);
  }

  /**
   * Should create key store.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldCreateKeyStore() throws Exception {
    String keyStore = KEY_STORE + RFC_TEST + ".pfx";
    SatClient client = new SatClient()
        .id(1)
        .rfc(RFC_TEST)
        .keystore(keyStore)
        .passwordPlain(RFC_TEST_PASS);

    doNothing().when(satClientRepository).deleteAllByRfc(anyString());
    when(satClientRepository.save(any(SatClient.class))).thenReturn(client);

    File keyFile = loadResource("PF_CFDI/" + RFC_TEST + ".key");
    MockMultipartFile key = new MockMultipartFile(
        "file-key",
        keyFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(keyFile)
    );

    File certFile = loadResource("PF_CFDI/" + RFC_TEST + ".cer");
    MockMultipartFile cert = new MockMultipartFile(
        "file-cert",
        certFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(certFile)
    );

    Map<String, String> res = controller.handleFileUpload(key, cert, RFC_TEST, RFC_TEST_PASS);
    assertThat(res.get("message")).isEqualTo("Keystore created for " + RFC_TEST);
  }

  @Test
  public void shouldFailedCreateKeyStore() throws Exception {
    String keyStore = KEY_STORE + RFC_TEST + ".pfx";
    SatClient client = new SatClient()
        .id(null)
        .rfc(RFC_TEST)
        .keystore(keyStore)
        .passwordPlain(RFC_TEST_PASS);

    doNothing().when(satClientRepository).deleteAllByRfc(anyString());
    when(satClientRepository.save(any(SatClient.class))).thenReturn(client);

    File keyFile = loadResource("PF_CFDI/" + RFC_TEST + ".key");
    MockMultipartFile key = new MockMultipartFile(
        "file-key",
        keyFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(keyFile)
    );

    File certFile = loadResource("PF_CFDI/" + RFC_TEST + ".cer");
    MockMultipartFile cert = new MockMultipartFile(
        "file-cert",
        certFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(certFile)
    );

    Throwable throwable = assertThrows(RuntimeException.class, () ->
        controller.handleFileUpload(key, cert, RFC_TEST, RFC_TEST_PASS)
    );

    assertThat(throwable.getMessage()).isEqualTo("Failed creating KeyStore");
  }

  @Test
  public void shouldFailedSavingPath() throws Exception {
    FileUtils.forceDelete(Path.of(KEY_STORE).toFile());
    doNothing().when(satClientRepository).deleteAllByRfc(anyString());
    doThrow(FielFileException.class).when(satClientRepository).save(any(SatClient.class));

    File keyFile = loadResource("PF_CFDI/" + RFC_TEST + ".key");
    MockMultipartFile key = new MockMultipartFile(
        "file-key",
        keyFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(keyFile)
    );

    File certFile = loadResource("PF_CFDI/" + RFC_TEST + ".cer");
    MockMultipartFile cert = new MockMultipartFile(
        "file-cert",
        certFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(certFile)
    );

    Throwable throwable = assertThrows(FielException.class, () ->
        controller.handleFileUpload(key, cert, RFC_TEST, RFC_TEST_PASS)
    );

    assertThat(throwable.getMessage()).isEqualTo("Failed handleFileUpload");
  }

  @Test
  public void shouldFailedWhenPFXCreatingFailed() throws Exception {
    File keystore = Path.of(KEY_STORE).toFile();
    if(keystore.exists()) {
      FileUtils.forceDelete(keystore);
    }

    FileUtils.forceMkdir(keystore);
    doNothing().when(satClientRepository).deleteAllByRfc(anyString());
    doThrow(FielFileException.class).when(satClientRepository).save(any(SatClient.class));

    File keyFile = loadResource("PF_CFDI/" + RFC_TEST + ".key");
    MockMultipartFile key = new MockMultipartFile(
        "file-key",
        keyFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(keyFile)
    );

    File certFile = loadResource("PF_CFDI/" + RFC_TEST + ".cer");
    MockMultipartFile cert = new MockMultipartFile(
        "file-cert",
        certFile.getName(),
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        new FileInputStream(certFile)
    );

    Map<String, String> error = controller.handleFileUpload(key, cert, RFC_TEST, "WRONG");

    assertThat(error.get("error")).isEqualTo("Keystore failed for " + RFC_TEST);
  }

  private Invoices buildMockInvoices() {
    Invoice invoice = new Invoice().complement(
            new Complemento().payments(new Payments().payment(
                    new Payment()
                        .amount(100.0)
                        .amount(100.0)
                        .currency("MXN")
                        .date(FORMATTER.format(ZonedDateTime.now().minusDays(4)))
                )
            )
        )
        .issuer(new Issuer()
            .name("SOME-COMP")
            .fiscalRegime("REGIME")
            .rfc("XXXX7001011XJ11")
        )
        .receptor(new Receptor()
            .name("SOME-INDIV")
            .rfc("XOJI740919U48")
        )
        .serial("SER-0011")
        .folio(UUID.randomUUID().toString());

    return Invoices.builder().invoices(
        Collections.singletonList(invoice)
    ).message("ALL GOOD").satState(StateCode.READY).build();
  }
}
