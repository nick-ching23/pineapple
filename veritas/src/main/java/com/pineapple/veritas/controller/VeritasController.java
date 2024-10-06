package com.pineapple.veritas.controller;

import com.pineapple.veritas.service.VeritasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    try {
      return veritasService.checkText(text);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping({"/checkTextUser"})
  public ResponseEntity<?> checkTextUser(@RequestParam String text, @RequestParam String userID, @RequestParam String orgID) {
    try {
      return veritasService.checkTextUser(text, userID, orgID);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @GetMapping({"/numFlags"})
  public ResponseEntity<?> numFlags(@RequestParam String userID, @RequestParam String orgID) {
    try {
      return veritasService.numFlags(userID, orgID);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  private ResponseEntity<?> handleException(Exception e) {
    System.out.println(e.toString());
    return new ResponseEntity<>("An error has occurred", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
