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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Local desktop console with the operating system look and feel.
 */
public final class AdminWindow implements AutoCloseable {

    @FunctionalInterface
    public interface Query {
        InspectionResult run(String input) throws Exception;
    }

    @FunctionalInterface
    public interface LocalCommands {
        boolean execute(String command, Consumer<String> output);
    }

    private final Query query;
    private final LocalCommands commands;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().daemon(true).name("tenio-admin-ui-query").factory()
    );
    private JFrame frame;
    private JDialog fullListDialog;
    private Timer refresh;
    private JTextArea output;
    private JTextField input;
    private CommandHistory commandHistory;
    private DefaultTableModel table;
    private Chart chart;
    private boolean busy;
    private String pending;

    public AdminWindow(String title, Query query, LocalCommands commands) {
        this.query = query;
        this.commands = commands;
        SwingUtilities.invokeLater(() -> open(title));
    }

    private void open(String title) {
        if (closed.get()) {
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException unavailable) {
            // Swing's installed default remains available.
        }
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                close();
            }
        });
        table = new DefaultTableModel(new Object[]{"Metric", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        chart = new Chart();
        chart.setBorder(BorderFactory.createTitledBorder("Connected players (last 60 samples)"));
        output = new JTextArea(8, 64);
        output.setEditable(false);
        input = new JTextField();
        input.setToolTipText("Press Up or Down to recall commands");
        input.addActionListener(event -> execute(input.getText().strip(), true));
        commandHistory = new CommandHistory(input);
        var tabs = new JTabbedPane();
        tabs.addTab("Statistics", new JScrollPane(new JTable(table)));
        tabs.addTab("Players", chart);
        var lower = new JPanel(new BorderLayout(4, 4));
        lower.add(new JScrollPane(output));
        var commandPanel = new JPanel(new BorderLayout(4, 4));
        commandPanel.add(new JLabel(AdminCommands.shortcuts()), BorderLayout.NORTH);
        commandPanel.add(input, BorderLayout.CENTER);
        lower.add(commandPanel, BorderLayout.SOUTH);
        var split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, lower);
        split.setResizeWeight(0.6);
        frame.add(split);
        frame.setSize(new Dimension(680, 500));
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        input.requestFocusInWindow();
        refresh = new Timer(5000, e -> {
            if ((frame.getExtendedState() & JFrame.ICONIFIED) == 0) {
                execute("stats", false);
            }
        });
        refresh.start();
        execute("stats", false);
    }

    private void execute(String command, boolean manual) {
        if (closed.get() || command.isBlank()) {
            return;
        }
        if (manual && AdminCommands.normalize(command).equals("clear")) {
            commandHistory.record(command);
            input.setText("");
            output.setText("");
            return;
        }
        if (busy && manual && pending == null) {
            pending = command;
            input.setEditable(false);
        }
        if (busy) {
            return;
        }
        busy = true;
        if (manual) {
            commandHistory.record(command);
            input.setText("");
            append("> " + command);
        }
        worker.execute(() -> {
            try {
                String normalized = AdminCommands.normalize(command);
                boolean fullHelp = normalized.equals("help all");
                boolean handled = manual && !AdminCommands.handles(command) && commands != null
                        && commands.execute(command, text -> SwingUtilities.invokeLater(() -> append(text)));
                if (normalized.equals("help") || !handled) {
                    var result = fullHelp ? fullHelp() : query.run(normalized);
                    SwingUtilities.invokeLater(() -> {
                        if (closed.get()) {
                            return;
                        }
                        if (normalized.equals("stats") || normalized.equals("server stats")) {
                            table.setRowCount(0);
                            for (int i = 0; i < result.labels().size(); i++) {
                                table.addRow(new Object[]{result.labels().get(i), result.values().get(i)});
                                if (result.labels().get(i).equals("Players")) {
                                    chart.addSample(Integer.parseInt(result.values().get(i).replace(",", "")));
                                }
                            }
                        } else if (manual && AdminCommands.opensFullList(normalized)
                                && !result.labels().equals(List.of("Result"))) {
                            showFullList(normalized, result);
                        } else if (manual) {
                            for (int i = 0; i < result.labels().size(); i++) {
                                append(result.labels().get(i) + ": " + result.values().get(i));
                            }
                            if (result.labels().isEmpty()) {
                                append("No entries");
                            }
                        }
                    });
                }
            } catch (Exception failure) {
                SwingUtilities.invokeLater(() -> append(failure.toString()));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    busy = false;
                    if (pending != null && !closed.get()) {
                        String next = pending;
                        pending = null;
                        input.setEditable(true);
                        execute(next, true);
                    }
                });
            }
        });
    }

    private InspectionResult fullHelp() throws Exception {
        var builtInHelp = query.run("help");
        var labels = new ArrayList<>(builtInHelp.labels());
        var values = new ArrayList<>(builtInHelp.values());
        if (commands != null) {
            commands.execute("help all", text -> {
                String[] details = text.split(" - ", 2);
                labels.add(details[0]);
                values.add(details.length == 2
                        ? details[1].lines().map(String::strip).collect(java.util.stream.Collectors.joining(" | "))
                        : "");
            });
        }
        return new InspectionResult(labels, values);
    }

    private void showFullList(String command, InspectionResult result) {
        if (fullListDialog != null) {
            fullListDialog.dispose();
        }

        var model = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return result.labels().size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int column) {
                return column == 0 ? "Item" : "Value";
            }

            @Override
            public Object getValueAt(int row, int column) {
                return column == 0 ? result.labels().get(row) : result.values().get(row);
            }
        };
        var results = new JTable(model);
        results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        results.getColumnModel().getColumn(0).setPreferredWidth(310);
        results.getColumnModel().getColumn(1).setPreferredWidth(520);
        var dialog = new JDialog(frame, "TenIO | " + command, false);
        fullListDialog = dialog;
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                if (fullListDialog == dialog) {
                    fullListDialog = null;
                }
            }
        });
        dialog.add(new JScrollPane(results));
        dialog.setSize(870, 450);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void append(String text) {
        if (!closed.get()) {
            if (output.getDocument().getLength() > 32000) {
                output.replaceRange("", 0, 16000);
            }
            output.append(text + System.lineSeparator());
            output.setCaretPosition(output.getDocument().getLength());
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            worker.shutdownNow();
            SwingUtilities.invokeLater(() -> {
                if (refresh != null) {
                    refresh.stop();
                }
                if (frame != null) {
                    frame.dispose();
                }
            });
        }
    }

    private static final class Chart extends JPanel {

        private final ArrayDeque<Integer> samples = new ArrayDeque<>();
        private static final int SAMPLE_LIMIT = 60;
        private static final int TARGET_TICKS = 4;

        void addSample(int count) {
            if (samples.size() == SAMPLE_LIMIT) {
                samples.removeFirst();
            }
            samples.addLast(count);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int peak = Math.max(1, samples.stream().mapToInt(Integer::intValue).max().orElse(0));
            long step = Math.ceilDiv((long) peak, TARGET_TICKS);
            long maximum = Math.ceilDiv(peak, step) * step;
            var formatter = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US);
            var font = g.getFontMetrics();
            int left = Math.max(font.stringWidth("Players"), font.stringWidth(formatter.format(maximum))) + 16;
            int right = getWidth() - 14;
            int top = getInsets().top + font.getHeight() + 8;
            int bottom = getHeight() - font.getDescent() - 8;
            if (right <= left || bottom <= top) {
                return;
            }

            g.setColor(getForeground());
            g.drawString("Players", 6, top - 8);
            g.drawLine(left, top, left, bottom);
            g.drawLine(left, bottom, right, bottom);
            for (long count = 0; count <= maximum; count += step) {
                int y = bottom - (int) Math.round((double) count / maximum * (bottom - top));
                String label = formatter.format(count);
                g.drawLine(left - 4, y, left, y);
                g.drawString(label, left - 8 - font.stringWidth(label), y + font.getAscent() / 2);
            }

            int index = 0;
            int previousX = 0;
            int previousY = 0;
            for (int count : samples) {
                int x = left + index * (right - left) / (SAMPLE_LIMIT - 1);
                int y = bottom - (int) Math.round((double) count / maximum * (bottom - top));
                if (index == 0) {
                    g.fillOval(x - 2, y - 2, 4, 4);
                } else {
                    g.drawLine(previousX, previousY, x, y);
                }
                index++;
                previousX = x;
                previousY = y;
            }
        }
    }
}
