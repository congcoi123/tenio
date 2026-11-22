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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.BitSet;

/**
 * Five numeric cron fields: minute, hour, day of month, month and weekday (Sunday 0 or 7).
 */
final class CronSchedule {

    private final BitSet minute;
    private final BitSet hour;
    private final BitSet day;
    private final BitSet month;
    private final BitSet weekday;
    private final boolean restrictBothDays;
    private final ZoneId zone;

    CronSchedule(String expression, String zoneName) {
        String[] fields = expression.trim().split("\\s+");
        if (fields.length != 5) {
            throw new IllegalArgumentException("Expected five cron fields: " + expression);
        }
        minute = parse(fields[0], 0, 59);
        hour = parse(fields[1], 0, 23);
        day = parse(fields[2], 1, 31);
        month = parse(fields[3], 1, 12);
        weekday = parse(fields[4], 0, 7);
        if (weekday.get(7)) {
            weekday.set(0);
        }
        restrictBothDays = !fields[2].startsWith("*") && !fields[4].startsWith("*");
        zone = ZoneId.of(zoneName);
    }

    private static BitSet parse(String field, int min, int max) {
        BitSet values = new BitSet(max + 1);
        for (String part : field.split(",", -1)) {
            String[] stepped = part.split("/", -1);
            String[] range = stepped[0].split("-", -1);
            boolean wildcard = stepped[0].equals("*");
            int first = wildcard ? min : Integer.parseInt(range[0]);
            int last = wildcard || stepped.length == 2 ? max : first;
            if (range.length == 2) {
                last = Integer.parseInt(range[1]);
            }
            int step = stepped.length == 2 ? Integer.parseInt(stepped[1]) : 1;
            if (range.length > 2 || stepped.length > 2 || step < 1 || first < min || last > max || last < first) {
                throw new IllegalArgumentException("Invalid cron field: " + field);
            }
            for (long value = first; value <= last; value += step) {
                values.set((int) value);
            }
        }
        return values;
    }

    Instant nextAfter(Instant now) {
        ZonedDateTime cursor = now.atZone(zone).truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        // Eight years include the longest gap between leap days around a non-leap century.
        LocalDate limit = cursor.toLocalDate().plusYears(8).plusDays(1);
        while (cursor.toLocalDate().isBefore(limit)) {
            boolean dom = day.get(cursor.getDayOfMonth());
            boolean dow = weekday.get(cursor.getDayOfWeek().getValue() % 7);
            boolean matchesDay = restrictBothDays ? dom || dow : dom && dow;
            if (!month.get(cursor.getMonthValue()) || !matchesDay) {
                cursor = cursor.toLocalDate().plusDays(1).atStartOfDay(zone);
            } else if (hour.get(cursor.getHour()) && minute.get(cursor.getMinute())) {
                return cursor.toInstant();
            } else {
                cursor = cursor.plusMinutes(1);
            }
        }
        throw new IllegalArgumentException("Cron expression has no matching date");
    }
}
