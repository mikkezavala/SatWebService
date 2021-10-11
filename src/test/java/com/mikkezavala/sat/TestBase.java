package com.mikkezavala.sat;

import java.io.File;
import java.io.FileNotFoundException;
import org.springframework.util.ResourceUtils;

abstract public class TestBase {

  public File loadResource(String file) throws FileNotFoundException {
    return ResourceUtils.getFile("classpath:" + file);
  }
}
