package com.pineapple.veritas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pineapple.veritas.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This class contains unit tests for the Organization class.
 */
@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = "external.api.url=http://mocked-url")
public class VeritasOrganizationTests {
  /**
   * Sets up testOrg for testing purposes.
   */
  @BeforeEach
  public void setupOrganizationForTesting() {
    testOrganization = new Organization();
    testOrganization.setOrgId("Org");
    testOrganization.setPassword("Password");
  }

  @Test
  public void orgIdTest() {
    assertEquals("Org", testOrganization.getOrgId());
  }

  @Test
  public void userIdTest() {
    assertEquals("Password", testOrganization.getPassword());
  }

  public static Organization testOrganization;
}
