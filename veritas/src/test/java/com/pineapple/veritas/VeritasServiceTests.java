package com.pineapple.veritas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pineapple.veritas.entity.Organization;
import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.mapper.OrganizationMapper;
import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.request.LoginRequest;
import com.pineapple.veritas.response.CheckTextResponse;
import com.pineapple.veritas.service.VeritasService;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * This class contains unit tests for the VeritasService class.
 */
@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = "external.api.url=http://mocked-url")
public class VeritasServiceTests {
  @Autowired
  private VeritasService veritasService;

  @MockBean
  private RecordMapper recordMapper;

  @MockBean
  private OrganizationMapper organizationMapper;

  @MockBean
  private SqlSessionTemplate sqlSessionTemplate;

  @MockBean
  private WebClient.Builder webClientBuilder;

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  /**
   * Sets up the mocking pipeline for every step of the client request.
   */
  @BeforeEach
  public void setUp() {
    requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    requestBodySpec = mock(WebClient.RequestBodySpec.class);
    requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClientBuilder.build()).thenReturn(webClient);

    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  public void testCheckTextNull() {
    ResponseEntity<Boolean> response = veritasService.checkText(null);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    response = veritasService.checkText("");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testCheckTextWebClientException() {
    when(webClient.post()).thenThrow(new WebClientResponseException(
        HttpStatus.BAD_REQUEST.value(), "Webclient Exception", null, null, null));
    ResponseEntity<Boolean> response = veritasService.checkText("Webclient Exception");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testCheckTextException() {
    when(webClient.post()).thenThrow(new RuntimeException("Exception"));
    ResponseEntity<Boolean> response = veritasService.checkText("Exception");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void testCheckText() {
    CheckTextResponse checkTextResponse = new CheckTextResponse();
    checkTextResponse.setResult(true);

    Mono<CheckTextResponse> monoResponse = Mono.just(checkTextResponse);

    when(responseSpec.bodyToMono(CheckTextResponse.class)).thenReturn(monoResponse);

    ResponseEntity<Boolean> response = veritasService.checkText("Some text");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody());
  }

  @Test
  public void testCheckTextUserNull() {
    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", null, "Not Null");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    response = veritasService.checkTextUser("Some Text", "Not Null", null);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    response = veritasService.checkTextUser("Some Text", "", "Not Null");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    response = veritasService.checkTextUser("Some Text", "Not Null", "");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testCheckTextUserNotOk() {
    when(webClient.post()).thenThrow(new RuntimeException("Some Error"));

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Error while checking text", response.getBody());
  }

  @Test
  public void testCheckTextUserNew() {
    CheckTextResponse checkTextResponse = new CheckTextResponse();
    checkTextResponse.setResult(false);

    Mono<CheckTextResponse> monoResponse = Mono.just(checkTextResponse);

    when(responseSpec.bodyToMono(CheckTextResponse.class)).thenReturn(monoResponse);

    when(recordMapper.selectByMap(any())).thenReturn(Collections.emptyList());

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertInstanceOf(Map.class, response.getBody());
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals(false, responseBody.get("flagged"));
    assertEquals("Text is not flagged.", responseBody.get("message"));

    checkTextResponse = new CheckTextResponse();
    checkTextResponse.setResult(true);

    monoResponse = Mono.just(checkTextResponse);

    when(responseSpec.bodyToMono(CheckTextResponse.class)).thenReturn(monoResponse);

    response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertInstanceOf(Map.class, response.getBody());
    responseBody = (Map<String, Object>) response.getBody();
    assertEquals(true, responseBody.get("flagged"));
    assertEquals("Text has been flagged and recorded.", responseBody.get("message"));
  }


  @Test
  public void testNumFlagsEmpty() {
    when(recordMapper.selectByMap(any())).thenReturn(Collections.emptyList());
    ResponseEntity<?> response = veritasService.numFlags("Some User", "Some Org");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody());
  }

  @Test
  public void testNumFlags() {
    Record mockRecord = new Record();
    when(recordMapper.selectByMap(any())).thenReturn(List.of(mockRecord));

    ResponseEntity<?> response = veritasService.numFlags("Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody());
  }

  @Test
  public void testRegister() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setOrgId("OrgId");
    loginRequest.setPassword("Password");
    ResponseEntity<?> response = veritasService.register(loginRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Successfully registered", response.getBody());
  }

  @Test
  public void testLoginEmpty() {

    when(organizationMapper.selectByMap(any())).thenReturn(Collections.emptyList());

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setOrgId("OrgId");
    loginRequest.setPassword("Password");
    ResponseEntity<?> response = veritasService.login(loginRequest);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("User or Password Incorrect", response.getBody());
  }

  @Test
  public void testLogin() {

    Organization mockOrganization = new Organization();
    mockOrganization.setOrgId("OrgId");
    mockOrganization.setPassword("Password");
    when(organizationMapper.selectByMap(any())).thenReturn(List.of(mockOrganization));

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setOrgId("OrgId");
    loginRequest.setPassword("Password");
    ResponseEntity<?> response = veritasService.login(loginRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockOrganization, response.getBody());
  }

  @Test
  public void testCheckRegistered() {

    Organization mockOrganization = new Organization();
    mockOrganization.setOrgId("OrgId");
    mockOrganization.setPassword("Password");

    when(organizationMapper.selectByMap(any())).thenReturn(List.of(mockOrganization));

    boolean response = veritasService.checkRegistered("OrgId");
    assertTrue(response);

    when(organizationMapper.selectByMap(any())).thenReturn(Collections.emptyList());

    response = veritasService.checkRegistered("OrgId");
    assertFalse(response);
  }

  @Test
  public void testIsTimeStampValidNull() {
    boolean response = veritasService.isTimeStampValid("NULL");
    assertFalse(response);
  }

  @Test
  public void testIsTimeStampValidExpired() throws NoSuchFieldException, IllegalAccessException {
    Map<String, Instant> originalTimestampMap;

    Instant instant1980 = ZonedDateTime.of(1980, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    Map<String, Instant> mockTimestampMap = mock(Map.class);
    when(mockTimestampMap.get(anyString())).thenReturn(instant1980);

    Field field = VeritasService.class.getDeclaredField("timestampMap");
    field.setAccessible(true);

    originalTimestampMap = (Map<String, Instant>) field.get(veritasService);
    field.set(veritasService, mockTimestampMap);

    veritasService.updateTimestamp("Expired");
    boolean response = veritasService.isTimeStampValid("Expired");
    assertFalse(response);

    field.set(veritasService, originalTimestampMap);
  }

  @Test
  public void testIsTimeStampValid() {
    veritasService.updateTimestamp("new");
    boolean response = veritasService.isTimeStampValid("new");
    assertTrue(response);
  }

}
