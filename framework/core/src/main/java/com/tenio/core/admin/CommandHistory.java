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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;

/**
 * In-memory command recall for the current console window.
 */
final class CommandHistory {

    private static final int LIMIT = 100;
    private final ArrayList<String> entries = new ArrayList<>();
    private final JTextField input;
    private int position;
    private String draft = "";

    CommandHistory(JTextField input) {
        this.input = input;
        bind(KeyEvent.VK_UP, "previous-command", -1);
        bind(KeyEvent.VK_DOWN, "next-command", 1);
    }

    void record(String command) {
        if (entries.isEmpty() || !entries.getLast().equals(command)) {
            if (entries.size() == LIMIT) {
                entries.removeFirst();
            }
            entries.add(command);
        }
        position = entries.size();
        draft = "";
    }

    private void bind(int key, String name, int direction) {
        input.getInputMap().put(KeyStroke.getKeyStroke(key, 0), name);
        input.getActionMap().put(name, new AbstractAction() {

            @Serial
            private static final long serialVersionUID = 2013527878408300821L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (!input.isEditable()) {
                    return;
                }
                int next = position + direction;
                if (next < 0 || next > entries.size()) {
                    return;
                }
                if (position == entries.size()) {
                    draft = input.getText();
                }
                position = next;
                input.setText(position == entries.size() ? draft : entries.get(position));
                input.setCaretPosition(input.getDocument().getLength());
            }
        });
    }
}
