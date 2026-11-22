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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CronScheduleTest {

  @Test
  void skipsMissingLocalTimeAndIncludesRepeatedLocalTime() {
    var cron = new CronSchedule("30 2 * * *", "Europe/Berlin");
    assertEquals(Instant.parse("2026-03-30T00:30:00Z"),
        cron.nextAfter(Instant.parse("2026-03-28T03:00:00Z")));
    assertEquals(Instant.parse("2026-10-25T01:30:00Z"),
        cron.nextAfter(Instant.parse("2026-10-25T00:30:00Z")));
  }

  @Test
  void rejectsImpossibleDatesAndZeroSteps() {
    assertThrows(IllegalArgumentException.class, () -> new CronSchedule("*/0 * * * *", "UTC"));
    var cron = new CronSchedule("0 0 31 2 *", "UTC");
    assertThrows(IllegalArgumentException.class, () -> cron.nextAfter(Instant.EPOCH));
  }

  @Test
  void combinesWeekdayListsHourRangesAndMinuteSteps() {
    var cron = new CronSchedule("*/15 8-10 * * 1,3,5", "UTC");
    assertEquals(Instant.parse("2026-11-23T08:00:00Z"),
        cron.nextAfter(Instant.parse("2026-11-20T10:45:00Z")));
  }

  @Test
  void restrictedDaysMatchEitherColumnAndSundayAcceptsSeven() {
    var cron = new CronSchedule("0 0 1 * 7", "UTC");
    assertEquals(Instant.parse("2026-11-22T00:00:00Z"),
        cron.nextAfter(Instant.parse("2026-11-20T10:45:00Z")));
    assertEquals(Instant.parse("2026-12-01T00:00:00Z"),
        cron.nextAfter(Instant.parse("2026-11-29T00:00:00Z")));
  }
}
