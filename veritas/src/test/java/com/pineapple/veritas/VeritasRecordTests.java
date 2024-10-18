package com.pineapple.veritas;

import com.pineapple.veritas.entity.Record;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = "external.api.url=http://mocked-url")
public class VeritasRecordTests {

  @BeforeEach
  public void setupRecordForTesting() {
    testRecord = new Record();
    testRecord.setOrgId("Org");
    testRecord.setUserId("User");
    testRecord.setNumFlags(1);
  }

  @Test
  public void orgIDTest() {
    assertEquals("Org", testRecord.getOrgId());
  }

  @Test
  public void userIDTest() {
    assertEquals("User", testRecord.getUserId());
  }

  @Test
  public void numFlagsTest() {
    assertEquals(1, testRecord.getNumFlags());
  }

  public static Record testRecord;
}
