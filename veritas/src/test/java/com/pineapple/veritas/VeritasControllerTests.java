package com.pineapple.veritas;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pineapple.veritas.controller.VeritasController;
import com.pineapple.veritas.mapper.OrganizationMapper;
import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.request.LoginRequest;
import com.pineapple.veritas.service.VeritasService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * This class contains unit tests for the VeritasController class.
 */
@WebMvcTest(VeritasController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "external.api.url=http://mocked-url")
public class VeritasControllerTests {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private VeritasService veritasService;

  @MockBean
  private RecordMapper recordMapper;

  @MockBean
  private OrganizationMapper organizationMapper;

  @MockBean
  private SqlSessionTemplate sqlSessionTemplate;

  @Test
  public void welcomeTest() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk())
        .andExpect(content().string("Welcome to Veritas!"));
    mockMvc.perform(get("/index")).andExpect(status().isOk())
        .andExpect(content().string("Welcome to Veritas!"));
    mockMvc.perform(get("/home")).andExpect(status().isOk())
        .andExpect(content().string("Welcome to Veritas!"));
  }

  @Test
  public void testCheckText() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
    Mockito.<ResponseEntity<?>>when(veritasService.checkText(anyString()))
        .thenReturn(response);

    mockMvc.perform(post("/checkText")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Sample text\""))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  public void testCheckTextUserNotRegistered() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);

    mockMvc.perform(post("/checkTextUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Sample text\"")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("Please register first"));
  }

  @Test
  public void testCheckTextUserNotValid() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
    Mockito.when(veritasService.checkRegistered(anyString())).thenReturn(true);

    mockMvc.perform(post("/checkTextUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Sample text\"")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("Session expired, please login again"));
  }

  @Test
  public void testCheckTextUser() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
    Mockito.when(veritasService.checkRegistered(anyString())).thenReturn(true);
    Mockito.when(veritasService.isTimeStampValid(anyString())).thenReturn(true);
    Mockito.<ResponseEntity<?>>when(veritasService.checkTextUser(anyString(),
            anyString(), anyString())).thenReturn(response);

    mockMvc.perform(post("/checkTextUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Sample text\"")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  public void testCheckNumFlagsNotRegistered() throws Exception {
    mockMvc.perform(get("/numFlags")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("Please register first"));
  }

  @Test
  public void testCheckNumFlagsNotValid() throws Exception {
    Mockito.when(veritasService.checkRegistered(anyString())).thenReturn(true);
    mockMvc.perform(get("/numFlags")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("Session expired, please login again"));
  }

  @Test
  public void testCheckNumFlags() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
    Mockito.when(veritasService.checkRegistered(anyString())).thenReturn(true);
    Mockito.when(veritasService.isTimeStampValid(anyString())).thenReturn(true);
    Mockito.<ResponseEntity<?>>when(veritasService.numFlags(anyString(), anyString()))
        .thenReturn(response);

    mockMvc.perform(get("/numFlags")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  public void testHandleException() throws Exception {
    when(veritasService.checkText(anyString()))
        .thenThrow(new RuntimeException("TestException"));
    mockMvc.perform(post("/checkText")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Sample text\""))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("An error has occurred"));
  }

  @Test
  public void testRegister() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<>(
            "Successfully registered", HttpStatus.OK);
    Mockito.<ResponseEntity<?>>when(
            veritasService.register(Mockito.any(LoginRequest.class))).thenReturn(response);

    LoginRequest loginRequest = new LoginRequest();
    String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequestJson))
        .andExpect(status().isOk())
        .andExpect(content().string("Successfully registered"));
  }

  @Test
  public void testLogin() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);
    Mockito.<ResponseEntity<?>>when(
            veritasService.login(Mockito.any(LoginRequest.class))).thenReturn(response);

    LoginRequest loginRequest = new LoginRequest();
    String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequestJson))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
  }
}
