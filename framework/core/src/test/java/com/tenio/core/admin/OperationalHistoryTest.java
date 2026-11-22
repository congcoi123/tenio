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

package com.tenio.core.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalHistoryTest {

    @Test
    void blockedExporterCannotBlockRecordingOrGrowHistory() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var history = new OperationalHistory(2)) {
            history.enableExport(1, event -> {
                entered.countDown();
                try {
                    release.await();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            });
            history.record(1, "TCP", OperationalHistory.Kind.DISCONNECT, "idle");
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            history.record(2, "TCP", OperationalHistory.Kind.RECONNECT, "resumed");
            history.record(3, "TCP", OperationalHistory.Kind.DISCONNECT, "idle");
            assertEquals(1, history.droppedExports());
            assertEquals(2, history.snapshot().getFirst().sessionId());
        } finally {
            release.countDown();
        }
    }

    @Test
    void fileModeDrainsEventsAndKeepsNoSnapshot(@TempDir Path directory) throws Exception {
        var history = OperationalHistory.toFile(directory);
        try (history) {
            history.record(8, "TCP", OperationalHistory.Kind.DISCONNECT, "EXCEPTION");
            history.record(9, "TCP", OperationalHistory.Kind.RECONNECT, "resumed");
            assertTrue(history.snapshot().isEmpty());
        }
        var rows = Files.readAllLines(history.file());
        assertEquals(3, rows.size());
        assertTrue(rows.get(1).endsWith("\t8\tTCP\tDISCONNECT\tEXCEPTION"));
        assertTrue(rows.get(2).endsWith("\t9\tTCP\tRECONNECT\tresumed"));
        assertEquals(0, history.droppedExports());
    }
}
