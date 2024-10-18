package com.pineapple.veritas;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pineapple.veritas.controller.VeritasController;
import com.pineapple.veritas.mapper.RecordMapper;
import com.pineapple.veritas.service.VeritasService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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

  @MockBean
  private VeritasService veritasService;

  @MockBean
  private RecordMapper recordMapper;

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

    mockMvc.perform(get("/checkText")
            .param("text", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  public void testCheckTextUser() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
    Mockito.<ResponseEntity<?>>when(veritasService.checkTextUser(anyString(),
            anyString(), anyString())).thenReturn(response);

    mockMvc.perform(post("/checkTextUser")
            .param("text", "Sample text")
            .param("orgId", "Sample text")
            .param("userId", "Sample text"))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  public void testCheckNumFlags() throws Exception {
    ResponseEntity<Integer> response = new ResponseEntity<>(1, HttpStatus.OK);
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

    mockMvc.perform(get("/checkText")
            .param("text", "Sample text"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("An error has occurred"));
  }


}
