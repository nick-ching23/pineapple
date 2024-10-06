package com.pineapple.veritas.controller;

import com.pineapple.veritas.service.VeritasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
  public ResponseEntity<?> checkTextUser(@RequestParam String text, @RequestParam String userID, @RequestParam String orgID) {
    return veritasService.checkTextUser(text, userID, orgID);
  }

  @GetMapping({"/numFlags"})
  public ResponseEntity<?> numFlags(@RequestParam String userID, @RequestParam String orgID) {
    return veritasService.numFlags(userID, orgID);
  }
}
