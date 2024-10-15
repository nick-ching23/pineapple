package com.pineapple.veritas.service;

import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.mapper.RecordMapper;
import java.util.List;

import com.pineapple.veritas.response.CheckTextResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;

@Service
public class VeritasService {
  @Autowired
  RecordMapper recordMapper;

  @Autowired
  private WebClient.Builder webClientBuilder;

  @Value("${external.api.url}")
  private String modelUrl;

  public ResponseEntity<Boolean> checkText(String text) {
    if (text == null || text.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    WebClient webClient = webClientBuilder.build();
    String apiCall = modelUrl + "/api/check-text";

    try {
      Boolean response = webClient
          .post()
          .uri(apiCall)
          .header("Content-Type", "application/json")
          .bodyValue(Map.of("text", text))
          .retrieve()
          .bodyToMono(CheckTextResponse.class)
          .map(CheckTextResponse::getResult)
          .block();
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (WebClientResponseException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ResponseEntity<?> checkTextUser(String text, String userID, String orgID) {
    if (userID == null || userID.isEmpty()) {
      return new ResponseEntity<>("UserID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    if (orgID == null || orgID.isEmpty()) {
      return new ResponseEntity<>("OrgID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    ResponseEntity<Boolean> textRes;
    try {
      textRes = (ResponseEntity<Boolean>) checkText(text);
    } catch (Exception e) {
      return new ResponseEntity<>("Error while checking text: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    if (textRes.getStatusCode() != HttpStatus.OK) {
      return new ResponseEntity<>("Error while checking text", textRes.getStatusCode());
    }

    if (textRes == null || textRes.getBody() == null) {
      return new ResponseEntity<>("Invalid response from text verification service", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Boolean flagged = textRes.getBody();

    Map<String, Object> recordMap = new HashMap<>();
    recordMap.put("orgID", orgID);
    recordMap.put("userID", userID);
    List<Record> records = recordMapper.selectByMap(recordMap);
    if (records.isEmpty()) {
      Record record = new Record();
      record.setOrgID(orgID);
      record.setUserID(userID);
      if (Boolean.TRUE.equals(flagged)) {
        record.setNumFlags(1);
      } else {
        record.setNumFlags(0);
      }
      recordMapper.insert(record);
    } else if (Boolean.TRUE.equals(flagged)) {
      Record record = records.get(0);
      record.setNumFlags(record.getNumFlags() + 1);
      recordMapper.updateById(record);
    }
    return new ResponseEntity<>("Operation completed successfully", HttpStatus.OK);
  }

  public ResponseEntity<?> numFlags(String userID, String orgID) {
    Map<String, Object> recordMap = new HashMap<>();
    recordMap.put("orgID", orgID);
    recordMap.put("userID", userID);
    List<Record> records = recordMapper.selectByMap(recordMap);
    if (records.isEmpty()) {
      return new ResponseEntity<>(0, HttpStatus.OK);
    } else {
      Record record = records.get(0);
      return new ResponseEntity<>(record.getNumFlags(), HttpStatus.OK);
    }
  }
}
