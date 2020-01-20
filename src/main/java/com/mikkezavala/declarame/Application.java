package com.mikkezavala.declarame;

import com.mikkezavala.declarame.service.SatWebServiceClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);

  }

  @Bean
  CommandLineRunner lookup(SatWebServiceClient client) {
    return args -> {
      client.response();
    };
  }
}
