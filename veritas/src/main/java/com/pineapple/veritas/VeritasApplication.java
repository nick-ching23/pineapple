package com.pineapple.veritas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Veritas application class.
 */
@SpringBootApplication
@MapperScan("com.pineapple.veritas.mapper")
public class VeritasApplication {

  public static void main(String[] args) {
    SpringApplication.run(VeritasApplication.class, args);
  }

}
