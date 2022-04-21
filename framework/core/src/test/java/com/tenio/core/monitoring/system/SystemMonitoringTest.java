package com.tenio.core.monitoring.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SystemMonitoringTest {
  @Test
  void testNewInstance() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by newInstance()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    SystemMonitoring.newInstance();
  }

  @Test
  void testGetCpuUsage() {
    assertEquals(0.0, SystemMonitoring.newInstance().getCpuUsage());
  }

  @Test
  @Disabled
  void testCountRunningThreads() {
    assertEquals(7, SystemMonitoring.newInstance().countRunningThreads());
  }

  @Test
  @Disabled
  void testGetTotalMemory() {
    assertEquals(362807296L, SystemMonitoring.newInstance().getTotalMemory());
  }

  @Test
  void testGetFreeMemory() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by getFreeMemory()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    SystemMonitoring.newInstance().getFreeMemory();
  }

  @Test
  void testGetUsedMemory() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by getUsedMemory()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    SystemMonitoring.newInstance().getUsedMemory();
  }
}

