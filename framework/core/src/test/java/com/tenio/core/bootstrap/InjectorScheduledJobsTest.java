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

package com.tenio.core.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.fixture.injector.scheduled.ScheduledFixture;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InjectorScheduledJobsTest {

  @Test
  void rescanDropsOldJobsAndRegistersTheWiredInstanceWithoutStartingIt() throws Exception {
    var injector = new Injector();
    String fixtures = ScheduledFixture.class.getPackageName();
    injector.setProfiles(Set.of("world"));
    injector.scanPackages(null, fixtures);
    var previous = injector.getBean(ScheduledFixture.Dependency.class);
    injector.scanPackages(null, fixtures);
    var current = injector.getBean(ScheduledFixture.Dependency.class);
    assertEquals(1L, current.invoked.getCount());
    var jobs = injector.getScheduledJobs();
    try {
      jobs.start();
      assertTrue(current.invoked.await(5, TimeUnit.SECONDS));
      assertEquals(1L, previous.invoked.getCount());
    } finally {
      jobs.shutdown();
    }
  }
  @Test
  void worldDiscoversMaintenanceWithoutEnablingMetrics() throws Exception {
    var injector = new Injector();
    injector.setProfiles(Set.of("world"));
    injector.scanPackages(null, com.tenio.core.configuration.constant.CoreConstant.DEFAULT_SCHEDULER_PACKAGE);
    org.junit.jupiter.api.Assertions.assertNotNull(injector.getBean(com.tenio.core.scheduler.job.MaintenanceJob.class));
    org.junit.jupiter.api.Assertions.assertNull(injector.getBean(com.tenio.core.scheduler.job.MetricReportJob.class));
  }
}
