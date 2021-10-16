package com.mikkezavala.sat.controller;

import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Complemento;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Issuer;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Payment;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Payments;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Receptor;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.service.sat.IndivContributorService;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class IndivContributorControllerTest extends TestBase {

  @Mock
  private IndivContributorService service;

  @InjectMocks
  private IndivContributorController controller;

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
