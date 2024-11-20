package com.pineapple.veritas.service;

import com.pineapple.veritas.entity.Organization;
import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.mapper.OrganizationMapper;
import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.request.LoginRequest;
import com.pineapple.veritas.response.CheckTextResponse;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Represents the core functionality of the Veritas service.
 */
@Service
public class VeritasService {
  @Autowired
  RecordMapper recordMapper;

  @Autowired
  OrganizationMapper orgMapper;

  private final Map<String, Instant> timestampMap = new HashMap<>();

  @Autowired
  private WebClient.Builder webClientBuilder;

  @Value("${external.api.url}")
  private String modelUrl;

  /**
   * Determines if the provided text may have been created using generative AI. This is done by
   * sending and waiting for the response from a separate request to a python flask endpoint
   * (running on a separate GCP VM) - which also makes use of OpenAI's API.
   *
   * @param text  String containing the text to be analyzed
   * @return Http response with a boolean indicating whether the provided text was potentially
   *     generated by generative AI or not (or an error).
   */
  public ResponseEntity<Boolean> checkText(String text) {
    if (text == null || text.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    WebClient webClient = webClientBuilder.build();
    String apiCall = modelUrl + "/api/check-text";

    try {
      Boolean response = webClient
          .post()
          .uri(URI.create(apiCall))
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

  /**
   * The same as checkText - except we also update the database to indicate the number of times this
   * particular user was flagged for AI-generated text.
   *
   * @param text String containing the text to be analyzed
   * @param userId String containing the userId (the user who is reponsible for this text)
   * @param orgId String containing the orgId (the organization to which the user belongs)
   * @return Http response with a boolean indicating whether the provided text was potentially
   *     generated by generative AI or not (or an error).
   */
  public ResponseEntity<?> checkTextUser(String text, String userId, String orgId) {
    if (userId == null || userId.isEmpty()) {
      return new ResponseEntity<>("UserID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    if (orgId == null || orgId.isEmpty()) {
      return new ResponseEntity<>("OrgID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    ResponseEntity<Boolean> textRes;
    textRes = (ResponseEntity<Boolean>) checkText(text);

    if (textRes.getStatusCode() != HttpStatus.OK) {
      return new ResponseEntity<>("Error while checking text", textRes.getStatusCode());
    }

    Boolean flagged = textRes.getBody();

    if (Boolean.TRUE.equals(flagged)) {
      Record record = new Record();
      record.setFlaggedText(text);
      record.setUserId(userId);
      record.setOrgId(orgId);
      recordMapper.insert(record);
    }

    return new ResponseEntity<>("Operation completed successfully", HttpStatus.OK);
  }

  /**
   * Checks the number of times a particular user has been flagged for AI-generated text (from DB).
   *
   * @param userId String containing the userId (the user we want to check)
   * @param orgId String containing the orgId (the organization to which the user belongs)
   * @return Http response with an integer indicating the number of times a user was flagged.
   */
  public ResponseEntity<?> numFlags(String userId, String orgId) {
    Map<String, Object> recordMap = new HashMap<>();
    recordMap.put("orgID", orgId);
    recordMap.put("userID", userId);
    List<Record> records = recordMapper.selectByMap(recordMap);
    if (records.isEmpty()) {
      return new ResponseEntity<>(0, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(records.size(), HttpStatus.OK);
    }
  }

  public ResponseEntity<?> register(LoginRequest loginRequest) {
    Organization org = new Organization();
    org.setOrgId(loginRequest.getOrgId());
    org.setPassword(loginRequest.getPassword());
    orgMapper.insert(org);
    return new ResponseEntity<>("Successfully registered", HttpStatus.OK);
  }

  public ResponseEntity<?> login(LoginRequest loginRequest) {
    Map<String, Object> loginMap = new HashMap<>();
    loginMap.put("orgId", loginRequest.getOrgId());
    loginMap.put("password", loginRequest.getPassword());
    List<Organization> orgs = orgMapper.selectByMap(loginMap);
    if (orgs.isEmpty()) {
      return new ResponseEntity<>("User or Password Incorrect", HttpStatus.BAD_REQUEST);
    }
    updateTimestamp(loginRequest.getOrgId());
    return new ResponseEntity<>(orgs.get(0), HttpStatus.OK);
  }

  public void updateTimestamp(String orgId) {
    timestampMap.put(orgId, Instant.now());
  }

  public boolean checkRegistered(String orgId) {
    Map<String, Object> loginMap = new HashMap<>();
    loginMap.put("orgId", orgId);
    List<Organization> orgs = orgMapper.selectByMap(loginMap);
    return !orgs.isEmpty();
  }

  public boolean isTimeStampValid(String orgId) {
    Instant lastLogin = timestampMap.get(orgId);
    if (lastLogin == null) {
      return false;
    }

    Instant now = Instant.now();
    long hoursElapsed = java.time.Duration.between(lastLogin, now).toHours();

    if (hoursElapsed >= 24) {
      timestampMap.remove(orgId);
      return false;
    }

    return true;
  }
}
