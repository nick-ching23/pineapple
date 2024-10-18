package com.pineapple.veritas;

import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.service.VeritasService;
import com.pineapple.veritas.response.CheckTextResponse;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    assertEquals("Operation completed successfully", response.getBody());

    checkTextResponse = new CheckTextResponse();
    checkTextResponse.setResult(false);

    monoResponse = Mono.just(checkTextResponse);

    when(responseSpec.bodyToMono(CheckTextResponse.class)).thenReturn(monoResponse);

    response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());
    verify(recordMapper, times(2)).insert(any(Record.class));
  }

  @Test
  public void testCheckTextUserOld() {
    CheckTextResponse checkTextResponse = new CheckTextResponse();
    checkTextResponse.setResult(true);

    Mono<CheckTextResponse> monoResponse = Mono.just(checkTextResponse);
    when(responseSpec.bodyToMono(CheckTextResponse.class)).thenReturn(monoResponse);

    Record mockRecord = new Record();
    mockRecord.setUserId("Some User");
    mockRecord.setOrgId("Some Org");
    mockRecord.setNumFlags(1);
    when(recordMapper.selectByMap(any())).thenReturn(Collections.singletonList(mockRecord));

    ResponseEntity<?> response = veritasService.checkTextUser("Some Text", "Some User", "Some Org");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Operation completed successfully", response.getBody());

    verify(recordMapper).updateByCompositeKey(eq("Some User"), eq("Some Org"), eq(2));
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
