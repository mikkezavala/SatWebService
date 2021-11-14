package com.mikkezavala.sat.service.sat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.template.SatWSTemplate;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.core.WebServiceTemplate;

@ExtendWith(SpringExtension.class)
public abstract class AbstractSatServiceTest extends TestBase {

  @Mock
  protected SatWSTemplate template;

  @Mock
  protected SatRepository repository;

  @BeforeEach
  protected void init() throws Exception {
    File pfxFile = loadResourceAsFile("PF_CFDI/" + RFC_TEST + ".p12");
    when(repository.tokenByRfc(anyString())).thenReturn(getMockToken());
    when(repository.satClientByRfc(anyString())).thenReturn(getMockedClient(pfxFile.getPath()));
  }
}
