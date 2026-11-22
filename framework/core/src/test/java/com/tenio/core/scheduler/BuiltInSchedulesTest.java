/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;

import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.Scheduled;
import com.tenio.core.bootstrap.injector.ProfileSelection;
import com.tenio.core.scheduler.job.MaintenanceJob;
import com.tenio.core.scheduler.job.MetricReportJob;
import org.junit.jupiter.api.Test;

class BuiltInSchedulesTest {

  @Test
  void builtInMonitoringSchedulesRequireMetricsAndUseSixtySecondDefaults() throws Exception {
    var world = new ProfileSelection(Set.of("world"));
    var metrics = new ProfileSelection(Set.of("world", "metrics"));
    var type = MetricReportJob.class;
    var component = type.getAnnotation(Component.class);
    assertEquals(false, world.matches(component.profile()));
    assertEquals(true, metrics.matches(component.profile()));
    for (String name : List.of("ccuReport", "deadlockScan", "systemMonitoring", "trafficCounter")) {
      var schedule = type.getMethod(name).getAnnotation(Scheduled.class);
      assertNotNull(schedule);
      assertEquals(60L, schedule.initialDelaySeconds());
      assertEquals(60L, schedule.intervalSeconds());
    }
  }

  @Test
  void maintenanceRunsWithTheBuiltInIntervals() throws Exception {
    var type = MaintenanceJob.class;
    assertEquals(30L, type.getMethod("emptyRooms").getAnnotation(Scheduled.class).intervalSeconds());
    assertEquals(45L, type.getMethod("idlePlayers").getAnnotation(Scheduled.class).intervalSeconds());
    assertEquals(45L, type.getMethod("orphanSessions").getAnnotation(Scheduled.class).intervalSeconds());
  }
}
