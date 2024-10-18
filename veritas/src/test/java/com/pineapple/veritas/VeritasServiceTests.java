package com.pineapple.veritas;

import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.service.VeritasService;
import com.pineapple.veritas.response.CheckTextResponse;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@SpringBootTest
@ContextConfiguration
public class VeritasServiceTests {
  @Autowired
  private VeritasService veritasService;

  @MockBean
  private RecordMapper recordMapper;

  @MockBean
  private SqlSessionTemplate sqlSessionTemplate;

  @MockBean
  private WebClient.Builder webClientBuilder;

  @MockBean
  private WebClient webClient;

  @BeforeEach
  public void setUp() {
    when(webClientBuilder.build()).thenReturn(webClient);
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

    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
    WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
    Mono<CheckTextResponse> monoResponse = Mono.just(checkTextResponse);

    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
  public void testCheckTextUserException() {
    when(veritasService.checkText("some text")).thenThrow(new RuntimeException("Some Error"));

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals("Error while checking text: Some Error", response.getBody());
  }

  @Test
  public void testCheckTextUserNotOk() {
    ResponseEntity<Boolean> textRes = new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Error while checking text", response.getBody());
  }

  @Test
  public void testCheckTextUserReturnsNull() {
    ResponseEntity<Boolean> textRes = null;
    when(veritasService.checkText("Some Text")).thenReturn(textRes);

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Invalid response from text verification service", response.getBody());

    textRes = new ResponseEntity<>(null, HttpStatus.OK);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);

    response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Invalid response from text verification service", response.getBody());
  }

  @Test
  public void testCheckTextUserNew() {
    ResponseEntity<Boolean> textRes = new ResponseEntity<>(false, HttpStatus.OK);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);
    when(recordMapper.selectByMap(any())).thenReturn(Collections.emptyList());

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());
    verify(recordMapper).insert(any(Record.class));

    textRes = new ResponseEntity<>(true, HttpStatus.OK);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);
    response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());
    verify(recordMapper).insert(any(Record.class));
  }

  @Test
  public void testCheckTextUserOld() {
    ResponseEntity<Boolean> textRes = new ResponseEntity<>(false, HttpStatus.OK);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);
    when(recordMapper.selectByMap(any())).thenReturn(List.of(new Record()));

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());
    verify(recordMapper).updateById(any(Record.class));

    textRes = new ResponseEntity<>(true, HttpStatus.OK);
    when(veritasService.checkText("Some Text")).thenReturn(textRes);
    response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());
    verify(recordMapper).updateById(any(Record.class));
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
    mockRecord.setNumFlags(42);
    when(recordMapper.selectByMap(any())).thenReturn(List.of(mockRecord));

    ResponseEntity<?> response = veritasService.numFlags("Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(42, response.getBody());
  }

}
