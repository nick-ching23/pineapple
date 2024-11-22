package com.pineapple.veritas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pineapple.veritas.entity.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This class contains unit tests for the Record class.
 */
@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = "external.api.url=http://mocked-url")
public class VeritasRecordTests {
  /**
   * Sets up testRecord for testing purposes.
   */
  @BeforeEach
  public void setupRecordForTesting() {
    testRecord = new Record();
    testRecord.setRecordId("Record");
    testRecord.setOrgId("Org");
    testRecord.setUserId("User");
    testRecord.setFlaggedText("Hi");
  }

  @Test
  public void recordIdTest() {
    assertEquals("Record", testRecord.getRecordId());
  }

  @Test
  public void flaggedTextTest() {
    assertEquals("Hi", testRecord.getFlaggedText());
  }

  @Test
  public void orgIdTest() {
    assertEquals("Org", testRecord.getOrgId());
  }

  @Test
  public void userIdTest() {
    assertEquals("User", testRecord.getUserId());
  }

  public static Record testRecord;
}
