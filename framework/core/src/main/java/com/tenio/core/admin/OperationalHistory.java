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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * Bounded instance-local history. Export callbacks run on a dedicated daemon worker.
 */
public final class OperationalHistory implements AutoCloseable {

    private static final int FILE_BACKLOG = 1024;

    public enum Kind {DISCONNECT, RECONNECT, PRESSURE_START, PRESSURE_END}

    public record Entry(long timestamp, UUID instance, long sessionId, String transport,
                        Kind kind, String reason) {
    }

    private final UUID instance = UUID.randomUUID();
    private final int capacity;
    private final ArrayDeque<Entry> entries;
    private final Path file;
    private final LongAdder lostExports = new LongAdder();
    private ThreadPoolExecutor worker;
    private Consumer<Entry> exporter;
    private boolean closed;

    public OperationalHistory(int capacity) {
        this(capacity, null);
    }

    private OperationalHistory(int capacity, Path file) {
        if (file == null && capacity < 1) {
            throw new IllegalArgumentException("History capacity must be positive");
        }
        this.capacity = capacity;
        this.file = file;
        entries = file == null ? new ArrayDeque<>() : null;
    }

    /**
     * Stores the event stream on disk with a bounded export queue and no in-memory history.
     */
    public static OperationalHistory toFile(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path path = Files.createTempFile(directory, "tenio-history-", ".tsv");
        Files.writeString(path, "timestamp\tinstance\tsession\ttransport\tevent\treason\n");
        var history = new OperationalHistory(0, path);
        history.enableExport(FILE_BACKLOG, history::appendToFile);
        return history;
    }

    /**
     * Returns the archive path in file mode, or null in memory mode.
     */
    public Path file() {
        return file;
    }

    private void appendToFile(Entry event) {
        String row = String.join("\t", Instant.ofEpochMilli(event.timestamp()).toString(),
                event.instance().toString(), Long.toString(event.sessionId()), escape(event.transport()),
                event.kind().name(), escape(event.reason()));
        try {
            Files.writeString(file, row + "\n", StandardOpenOption.APPEND);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static String escape(String value) {
        return Objects.toString(value, "").replace("\\", "\\\\").replace("\t", "\\t")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    public synchronized void enableExport(int backlog, Consumer<Entry> sink) {
        if (worker != null || closed) {
            throw new IllegalStateException("Exporter already configured or history closed");
        }
        exporter = Objects.requireNonNull(sink);
        worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(backlog),
                Thread.ofPlatform().daemon(true).name("tenio-history-export").factory());
    }

    public synchronized void record(long id, String transport, Kind kind, String reason) {
        if (closed) {
            return;
        }
        var entry = new Entry(System.currentTimeMillis(), instance, id, transport, kind, reason);
        if (entries != null) {
            if (entries.size() == capacity) {
                entries.removeFirst();
            }
            entries.addLast(entry);
        }
        if (worker != null) {
            try {
                worker.execute(() -> {
                    try {
                        exporter.accept(entry);
                    } catch (RuntimeException failure) {
                        lostExports.increment();
                        if (file != null && lostExports.sum() == 1) {
                            System.getLogger(OperationalHistory.class.getName())
                                    .log(System.Logger.Level.ERROR, "Writing operational history failed", failure);
                        }
                    }
                });
            } catch (RejectedExecutionException full) {
                lostExports.increment();
            }
        }
    }

    public synchronized List<Entry> snapshot() {
        return entries == null ? List.of() : List.copyOf(entries);
    }

    public long droppedExports() {
        return lostExports.sum();
    }

    @Override
    public void close() {
        ThreadPoolExecutor drain;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            if (entries != null) {
                entries.clear();
                if (worker != null) {
                    lostExports.add(worker.shutdownNow().size());
                }
                return;
            }
            drain = worker;
            drain.shutdown();
        }
        try {
            if (!drain.awaitTermination(5, TimeUnit.SECONDS)) {
                lostExports.add(drain.shutdownNow().size());
            }
        } catch (InterruptedException interrupted) {
            lostExports.add(drain.shutdownNow().size());
            Thread.currentThread().interrupt();
        }
        long lost = droppedExports();
        if (lost > 0) {
            System.getLogger(OperationalHistory.class.getName()).log(System.Logger.Level.WARNING,
                    "Operational history lost " + lost + " events while exporting to " + file);
        }
    }
}
