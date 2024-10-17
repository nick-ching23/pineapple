package com.pineapple.veritas.controller;

import com.pineapple.veritas.service.VeritasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Route controller for the Veritas service.
 */
@RestController
public class VeritasController {
  private final VeritasService veritasService;

  @Autowired
  public VeritasController(VeritasService veritasService) {
    this.veritasService = veritasService;
  }

  @GetMapping({"/", "/index", "/home"})
  public String index() {
    return "Welcome to Veritas!";
  }

  @GetMapping({"/checkText"})
  public ResponseEntity<?> checkText(@RequestParam String text) {
    return veritasService.checkText(text);
  }

  @PostMapping({"/checkTextUser"})
  public ResponseEntity<?> checkTextUser(@RequestParam String text, @RequestParam String userId,
                                         @RequestParam String orgId) {
    return veritasService.checkTextUser(text, userId, orgId);
  }

  @GetMapping({"/numFlags"})
  public ResponseEntity<?> numFlags(@RequestParam String userId, @RequestParam String orgId) {
    return veritasService.numFlags(userId, orgId);
  }
}
