package com.mikkezavala.sat.adapter;

import static com.mikkezavala.sat.util.Constant.DATE_TIME_PATTERN;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The type Zoned date time adapter.
 */
public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

  private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

  @Override
  public ZonedDateTime unmarshal(String v) {
    return ZonedDateTime.parse(v, FORMATTER);
  }

  @Override
  public String marshal(ZonedDateTime v) {
    return v.format(FORMATTER);
  }
}
