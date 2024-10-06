package com.pineapple.veritas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pineapple.veritas.entity")
public class VeritasApplication {

  public static void main(String[] args) {
    SpringApplication.run(VeritasApplication.class, args);
  }

}
