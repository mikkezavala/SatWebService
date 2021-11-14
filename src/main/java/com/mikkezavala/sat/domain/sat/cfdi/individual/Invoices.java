package com.mikkezavala.sat.domain.sat.cfdi.individual;

import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Invoices {

  private String message;

  private StateCode satState;

  @Default
  private List<Invoice> invoices = new ArrayList<>();
}
